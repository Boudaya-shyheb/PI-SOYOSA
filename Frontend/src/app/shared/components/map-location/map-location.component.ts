import { Component, EventEmitter, Input, OnInit, Output, OnDestroy, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import * as L from 'leaflet';

// Fix for Leaflet marker icons when using Webpack/Angular
const iconRetinaUrl = 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png';
const iconUrl = 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png';
const shadowUrl = 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png';
const iconDefault = L.icon({
  iconRetinaUrl,
  iconUrl,
  shadowUrl,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize: [41, 41]
});
L.Marker.prototype.options.icon = iconDefault;

@Component({
  selector: 'app-map-location',
  templateUrl: './map-location.component.html',
  styleUrls: ['./map-location.component.css']
})
export class MapLocationComponent implements OnInit, AfterViewInit, OnDestroy {
  @Input() latitude: number | null = null;
  @Input() longitude: number | null = null;
  @Input() readOnly: boolean = false;
  
  @Output() locationSelected = new EventEmitter<{lat: number, lng: number}>();
  
  @ViewChild('mapElement') mapElement!: ElementRef;
  
  private map!: L.Map;
  private marker: L.Marker | null = null;
  private userMarker: L.Marker | null = null;
  private routeLayer: L.GeoJSON | null = null;
  private watchId: number | null = null;
  private lastRouteLat: number | null = null;
  private lastRouteLng: number | null = null;
  
  public routingError: string | null = null;
  public isLocating: boolean = false;

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    // Slight timeout ensures DOM is fully painted before Leaflet initializes
    setTimeout(() => {
      this.initMap();
    }, 100);
  }

  ngOnDestroy(): void {
    if (this.watchId !== null) {
      navigator.geolocation.clearWatch(this.watchId);
    }
    if (this.map) {
      this.map.remove();
    }
  }

  private initMap(): void {
    // Default to a central location (e.g., Paris or user's provided location)
    const initialLat = this.latitude || 48.8566;
    const initialLng = this.longitude || 2.3522;
    const initialZoom = this.latitude && this.longitude ? 15 : 5;

    if (!this.mapElement || !this.mapElement.nativeElement) return;
    this.map = L.map(this.mapElement.nativeElement).setView([initialLat, initialLng], initialZoom);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);
    
    // Crucial for map showing up properly when initialized inside a hidden/ngIf tab
    // We try multiple times to be sure the container is ready
    const timer = setInterval(() => {
      if (this.map) {
        this.map.invalidateSize();
      }
    }, 500);
    setTimeout(() => clearInterval(timer), 3000);

    if (this.latitude && this.longitude) {
      this.marker = L.marker([this.latitude, this.longitude], {
        icon: L.icon({
          iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
          shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
          iconSize: [25, 41],
          iconAnchor: [12, 41],
          popupAnchor: [1, -34],
          className: 'hue-rotate-[140deg]' // Makes it look Red/Orange (Destination)
        })
      }).addTo(this.map).bindPopup('<b>Training Venue</b>');
    }

    if (!this.readOnly) {
      this.map.on('click', (e: L.LeafletMouseEvent) => {
        const lat = e.latlng.lat;
        const lng = e.latlng.lng;
        
        if (this.marker) {
          this.marker.setLatLng([lat, lng]);
        } else {
          this.marker = L.marker([lat, lng]).addTo(this.map);
        }
        
        this.locationSelected.emit({ lat, lng });
      });
    }
  }
  
  public getDirectionsOnMap(): void {
    if (!this.latitude || !this.longitude) return;
    
    this.isLocating = true;
    this.routingError = null;

    if (!navigator.geolocation) {
      this.routingError = 'Geolocation is not supported by your browser.';
      this.isLocating = false;
      return;
    }

    if (this.watchId !== null) {
      navigator.geolocation.clearWatch(this.watchId);
    }

    this.watchId = navigator.geolocation.watchPosition(
      (position) => {
        const userLat = position.coords.latitude;
        const userLng = position.coords.longitude;
        
        this.updateUserPosition(userLat, userLng);
      },
      (error) => {
        this.isLocating = false;
        switch(error.code) {
          case error.PERMISSION_DENIED:
            this.routingError = 'Please allow location access to keep track of your position.';
            break;
          case error.POSITION_UNAVAILABLE:
          case error.TIMEOUT:
            this.routingError = 'Could not determine your location.';
            break;
        }
      },
      { 
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0 
      }
    );
  }

  private updateUserPosition(userLat: number, userLng: number): void {
    // 1. If it's the first time locating, or significant movement (> 50m), redraw the route
    const shouldRedraw = this.shouldRedrawRoute(userLat, userLng);
    
    if (shouldRedraw) {
      this.drawRoute(userLat, userLng);
    } else {
      this.isLocating = false; // Done locating if we're not redrawing
    }

    // 2. Always update the user marker position smoothly
    if (this.userMarker) {
      this.userMarker.setLatLng([userLat, userLng]);
    } else {
      this.userMarker = L.marker([userLat, userLng], {
        icon: L.icon({
          iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
          shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
          iconSize: [25, 41],
          iconAnchor: [12, 41],
          popupAnchor: [1, -34]
          // Removed hue-rotate, making Student marker Blue (Standard)
        })
      }).addTo(this.map).bindPopup('<b>Your Current Location</b>').openPopup();
    }
  }

  private shouldRedrawRoute(lat: number, lng: number): boolean {
    if (this.lastRouteLat === null || this.lastRouteLng === null) return true;
    
    // Haversine-like distance check for ~50 meters approx
    const dLat = Math.abs(lat - this.lastRouteLat);
    const dLng = Math.abs(lng - this.lastRouteLng);
    
    // 0.00045 degrees is roughly 50 meters
    return (dLat > 0.00045 || dLng > 0.00045);
  }

  private drawRoute(userLat: number, userLng: number): void {
    const targetLat = this.latitude;
    const targetLng = this.longitude;

    if (!targetLat || !targetLng) return;
    
    // Remember last route anchor coordinates
    this.lastRouteLat = userLat;
    this.lastRouteLng = userLng;

    // Use free public OSRM mapping service to draw a route without an API key
    const osrmApiUrl = `https://router.project-osrm.org/route/v1/driving/${userLng},${userLat};${targetLng},${targetLat}?overview=full&geometries=geojson`;

    fetch(osrmApiUrl)
      .then(res => res.json())
      .then(data => {
        this.isLocating = false;
        if (data.routes && data.routes.length > 0) {
          // Clear older routes
          if (this.routeLayer) {
            this.map.removeLayer(this.routeLayer);
          }

          // Draw the route Polyline as GeoJSON
          const routeGeometry = data.routes[0].geometry;
          this.routeLayer = L.geoJSON(routeGeometry, {
            style: { color: '#2563eb', weight: 5, opacity: 0.8 }
          }).addTo(this.map);

          // On first load, fit boundaries. Subsequent updates just move markers to avoid jumping
          if (!this.userMarker) {
             const bounds = L.latLngBounds([userLat, userLng], [targetLat, targetLng]);
             this.map.fitBounds(bounds, { padding: [50, 50] });
          }
        } else {
          this.routingError = 'Could not find a driving route directly there.';
        }
      })
      .catch(err => {
        this.isLocating = false;
        this.routingError = 'Error connecting to routing service.';
        console.error('OSRM API Error:', err);
      });
  }

  public openExternalGoogleMaps(): void {
    if (this.latitude && this.longitude) {
      const url = `https://www.google.com/maps/dir/?api=1&destination=${this.latitude},${this.longitude}`;
      window.open(url, '_blank');
    }
  }
} 
