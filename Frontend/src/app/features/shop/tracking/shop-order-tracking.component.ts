import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import * as L from 'leaflet';
import { Subscription } from 'rxjs';
import { LocationUpdate, OrderTrackingService, TrackingStatus } from '../../../services/order-tracking.service';
import { EcommerceAdminApiService, OrderDto } from '../../../services/ecommerce-admin-api.service';
import { RoutingService, LatLng, RouteResult } from '../../../services/routing.service';

@Component({
  selector: 'app-shop-order-tracking',
  templateUrl: './shop-order-tracking.component.html',
  styleUrls: ['./shop-order-tracking.component.css']
})
export class ShopOrderTrackingComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('mapContainer') mapContainer?: ElementRef<HTMLDivElement>;

  orderId: number | null = null;
  status: TrackingStatus = 'disconnected';
  lastUpdate: LocationUpdate | null = null;
  errorMessage = '';
  order: OrderDto | null = null;
  orderLoading = false;
  orderError = '';
  deliveryAgentName = '';
  routeError = '';
  routeEtaSeconds: number | null = null;

  private map: L.Map | null = null;
  private marker: L.CircleMarker | null = null;
  private destinationMarker: L.CircleMarker | null = null;
  private routeLine: L.Polyline | null = null;
  private routeSegments: L.Polyline[] = [];
  private destinationCoords: LatLng | null = null;
  private lastRouteStart: LatLng | null = null;
  private lastRouteRequestedAt = 0;
  private mapReady = false;
  private subscription?: Subscription;
  private statusSubscription?: Subscription;
  private hasCentered = false;
  private routeRefreshMs = 5000;

  constructor(
    private route: ActivatedRoute,
    private tracking: OrderTrackingService,
    private ecommerce: EcommerceAdminApiService,
    private routing: RoutingService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('orderId');
    const parsed = idParam ? Number(idParam) : NaN;
    if (!Number.isFinite(parsed) || parsed <= 0) {
      this.errorMessage = 'Invalid order id.';
      return;
    }
    this.orderId = parsed;

    this.loadOrderDetails(parsed);
    this.loadDeliveryAgent(parsed);

    this.statusSubscription = this.tracking.status$.subscribe((status) => {
      this.status = status;
    });

    this.subscription = this.tracking.connect(this.orderId).subscribe((update) => {
      this.lastUpdate = update;
      this.updateMap(update);
    });
  }

  ngAfterViewInit(): void {
    if (!this.mapContainer) {
      return;
    }
    const initialLat = 36.8065;
    const initialLng = 10.1815;
    this.map = L.map(this.mapContainer.nativeElement, { zoomControl: true })
      .setView([initialLat, initialLng], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(this.map);

    this.marker = L.circleMarker([initialLat, initialLng], {
      radius: 8,
      color: '#0f766e',
      fillColor: '#14b8a6',
      fillOpacity: 0.9
    }).addTo(this.map);

    this.mapReady = true;

    if (this.lastUpdate) {
      this.updateMap(this.lastUpdate);
    }
    if (this.destinationCoords) {
      this.updateDestinationMarker(this.destinationCoords);
      this.updateRoute(true);
    }
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
    this.statusSubscription?.unsubscribe();
    this.tracking.disconnect();
    this.clearRouteSegments();
    this.routeLine?.remove();
    this.map?.remove();
  }

  private loadOrderDetails(orderId: number): void {
    this.orderLoading = true;
    this.orderError = '';
    this.ecommerce.getOrderById(orderId).subscribe({
      next: (response) => {
        this.order = response.data ?? null;
        this.orderLoading = false;
        this.resolveDestination();
      },
      error: (error) => {
        this.orderError = error?.error?.message || 'Unable to load order details.';
        this.orderLoading = false;
      }
    });
  }

  private loadDeliveryAgent(orderId: number): void {
    this.ecommerce.listCourierAssignments().subscribe({
      next: (response) => {
        const assignment = (response.data ?? []).find((item) => item.orderId === orderId);
        if (assignment?.courierName) {
          this.deliveryAgentName = assignment.courierName;
        } else if (assignment?.courierId) {
          this.deliveryAgentName = `Delivery agent #${assignment.courierId}`;
        }
      },
      error: () => {
        this.deliveryAgentName = '';
      }
    });
  }

  get deliveryAgentLabel(): string {
    if (this.deliveryAgentName) {
      return this.deliveryAgentName;
    }
    if (this.lastUpdate?.courierId) {
      return `Delivery agent #${this.lastUpdate.courierId}`;
    }
    return 'Not assigned';
  }

  private updateMap(update: LocationUpdate): void {
    if (!this.mapReady || !this.map || !this.marker) {
      return;
    }
    const coords: [number, number] = [update.lat, update.lng];
    this.marker.setLatLng(coords);
    if (!this.hasCentered) {
      this.map.setView(coords, 14);
      this.hasCentered = true;
    }
    this.updateRoute();
  }

  private resolveDestination(): void {
    this.routeError = '';
    if (this.order?.deliveryLat !== null && this.order?.deliveryLat !== undefined
      && this.order?.deliveryLng !== null && this.order?.deliveryLng !== undefined) {
      const coords: LatLng = [this.order.deliveryLat, this.order.deliveryLng];
      this.destinationCoords = coords;
      this.updateDestinationMarker(coords);
      this.updateRoute(true);
      return;
    }
    const address = this.buildDeliveryAddress();
    if (!address) {
      this.routeError = 'Delivery address is missing.';
      return;
    }
    this.routing.geocodeAddressParts(
      this.order?.deliveryStreet,
      this.order?.deliveryCity,
      this.order?.deliveryPostalCode,
      this.order?.deliveryCountry,
      address
    ).subscribe({
      next: (coords) => {
        if (!coords) {
          this.routeError = 'Unable to find the delivery address.';
          return;
        }
        this.destinationCoords = coords;
        this.updateDestinationMarker(coords);
        this.updateRoute(true);
      },
      error: () => {
        this.routeError = 'Unable to geocode the delivery address.';
      }
    });
  }

  private buildDeliveryAddress(): string | null {
    if (!this.order) {
      return null;
    }
    const parts = [
      this.order.deliveryStreet,
      this.order.deliveryCity,
      this.order.deliveryPostalCode,
      this.order.deliveryCountry
    ]
      .map((value) => (value ?? '').trim())
      .filter((value) => value.length > 0);

    return parts.length > 0 ? parts.join(', ') : null;
  }

  private updateDestinationMarker(coords: LatLng): void {
    if (!this.mapReady || !this.map) {
      return;
    }
    if (!this.destinationMarker) {
      this.destinationMarker = L.circleMarker(coords, {
        radius: 7,
        color: '#1d4ed8',
        fillColor: '#3b82f6',
        fillOpacity: 0.85
      }).addTo(this.map);
      return;
    }
    this.destinationMarker.setLatLng(coords);
  }

  private updateRoute(force = false): void {
    if (!this.mapReady || !this.map || !this.lastUpdate || !this.destinationCoords) {
      return;
    }

    const now = Date.now();
    if (!force && now - this.lastRouteRequestedAt < this.routeRefreshMs) {
      return;
    }

    const start: LatLng = [this.lastUpdate.lat, this.lastUpdate.lng];
    if (!force && this.lastRouteStart && !this.hasMovedEnough(start, this.lastRouteStart)) {
      return;
    }

    this.lastRouteRequestedAt = now;
    this.lastRouteStart = start;
    this.routing.getRoute(start, this.destinationCoords).subscribe({
      next: (result) => {
        if (!result || result.points.length === 0) {
          this.routeError = 'Unable to build route to destination.';
          return;
        }
        this.routeError = '';
        this.routeEtaSeconds = result.durationSeconds ?? null;
        this.renderRoute(result);
      },
      error: () => {
        this.routeError = 'Unable to build route to destination.';
      }
    });
  }

  get etaLabel(): string {
    if (!this.routeEtaSeconds) {
      return '-';
    }
    return this.formatDuration(this.routeEtaSeconds);
  }

  private renderRoute(result: RouteResult): void {
    if (!this.map) {
      return;
    }

    if (result.congestion && result.congestion.length > 0) {
      this.clearRouteSegments();
      if (this.routeLine) {
        this.routeLine.remove();
        this.routeLine = null;
      }

      const fallback = result.congestion[result.congestion.length - 1] ?? 'unknown';
      for (let i = 0; i < result.points.length - 1; i += 1) {
        const level = result.congestion[i] ?? fallback;
        const segment = L.polyline([result.points[i], result.points[i + 1]], {
          color: this.colorForCongestion(level),
          weight: 4,
          opacity: 0.95
        }).addTo(this.map);
        this.routeSegments.push(segment);
      }
      return;
    }

    this.clearRouteSegments();
    if (!this.routeLine) {
      this.routeLine = L.polyline(result.points, {
        color: '#0f766e',
        weight: 4,
        opacity: 0.9
      }).addTo(this.map);
    } else {
      this.routeLine.setLatLngs(result.points);
    }
  }

  private clearRouteSegments(): void {
    for (const segment of this.routeSegments) {
      segment.remove();
    }
    this.routeSegments = [];
  }

  private colorForCongestion(level: string): string {
    switch (level) {
      case 'low':
        return '#22c55e';
      case 'moderate':
        return '#f59e0b';
      case 'heavy':
        return '#ef4444';
      case 'severe':
        return '#7f1d1d';
      default:
        return '#0f766e';
    }
  }

  private formatDuration(seconds: number): string {
    const totalSeconds = Math.max(0, Math.round(seconds));
    const totalMinutes = Math.floor(totalSeconds / 60);
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    if (hours > 0) {
      return `${hours}h ${minutes}m`;
    }
    return `${minutes}m`;
  }

  private hasMovedEnough(current: LatLng, previous: LatLng): boolean {
    const latDiff = Math.abs(current[0] - previous[0]);
    const lngDiff = Math.abs(current[1] - previous[1]);
    return latDiff + lngDiff > 0.00008;
  }
}
