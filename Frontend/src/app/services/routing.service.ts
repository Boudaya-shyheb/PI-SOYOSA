import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../environments/environment';

export type LatLng = [number, number];

interface NominatimResult {
  lat: string;
  lon: string;
}

interface OsrmRouteResponse {
  routes?: Array<{
    geometry?: {
      coordinates?: [number, number][];
    };
    duration?: number;
  }>;
}

interface MapboxDirectionsResponse {
  routes?: Array<{
    geometry?: {
      coordinates?: [number, number][];
    };
    duration?: number;
    legs?: Array<{
      annotation?: {
        congestion?: string[];
      };
    }>;
  }>;
}

export interface ReverseGeocodeResult {
  road?: string;
  house_number?: string;
  city?: string;
  town?: string;
  village?: string;
  state?: string;
  postcode?: string;
  country?: string;
}

export interface RouteResult {
  points: LatLng[];
  durationSeconds?: number;
  congestion?: string[];
  source: 'mapbox' | 'osrm';
}

interface NominatimReverseResponse {
  address?: ReverseGeocodeResult;
}

@Injectable({
  providedIn: 'root'
})
export class RoutingService {
  private mapboxToken = environment.mapboxPublicToken || '';

  constructor(private http: HttpClient) {}

  geocodeAddress(address: string): Observable<LatLng | null> {
    const params = new HttpParams()
      .set('format', 'json')
      .set('q', address)
      .set('limit', '1');

    return this.http
      .get<NominatimResult[]>('https://nominatim.openstreetmap.org/search', { params })
      .pipe(map((results) => this.pickFirstLatLng(results)));
  }

  geocodeAddressParts(
    street?: string | null,
    city?: string | null,
    postalCode?: string | null,
    country?: string | null,
    fallbackQuery?: string | null
  ): Observable<LatLng | null> {
    let params = new HttpParams().set('format', 'json').set('limit', '1');

    const cleanStreet = (street ?? '').trim();
    const cleanCity = (city ?? '').trim();
    const cleanPostal = (postalCode ?? '').trim();
    const cleanCountry = (country ?? '').trim();
    const cleanFallback = (fallbackQuery ?? '').trim();

    if (cleanStreet) {
      params = params.set('street', cleanStreet);
    }
    if (cleanCity) {
      params = params.set('city', cleanCity);
    }
    if (cleanPostal) {
      params = params.set('postalcode', cleanPostal);
    }
    if (cleanCountry) {
      params = params.set('country', cleanCountry);
    }

    if (!cleanStreet && !cleanCity && !cleanPostal && !cleanCountry && cleanFallback) {
      params = params.set('q', cleanFallback);
    }

    return this.http
      .get<NominatimResult[]>('https://nominatim.openstreetmap.org/search', { params })
      .pipe(map((results) => this.pickFirstLatLng(results)));
  }

  getRoute(start: LatLng, end: LatLng): Observable<RouteResult | null> {
    if (this.mapboxToken && this.mapboxToken !== 'YOUR_MAPBOX_PUBLIC_TOKEN') {
      return this.getMapboxRoute(start, end);
    }
    return this.getOsrmRoute(start, end);
  }

  private getOsrmRoute(start: LatLng, end: LatLng): Observable<RouteResult | null> {
    const startLngLat = `${start[1]},${start[0]}`;
    const endLngLat = `${end[1]},${end[0]}`;
    const url = `https://router.project-osrm.org/route/v1/driving/${startLngLat};${endLngLat}`;
    const params = new HttpParams()
      .set('overview', 'full')
      .set('geometries', 'geojson');

    return this.http.get<OsrmRouteResponse>(url, { params }).pipe(
      map((response) => {
        const route = response?.routes?.[0];
        const points = this.mapRouteCoords(route?.geometry?.coordinates);
        if (!points || points.length === 0) {
          return null;
        }
        return {
          points,
          durationSeconds: route?.duration,
          source: 'osrm'
        } as RouteResult;
      })
    );
  }

  private getMapboxRoute(start: LatLng, end: LatLng): Observable<RouteResult | null> {
    const startLngLat = `${start[1]},${start[0]}`;
    const endLngLat = `${end[1]},${end[0]}`;
    const url = `https://api.mapbox.com/directions/v5/mapbox/driving-traffic/${startLngLat};${endLngLat}`;
    const params = new HttpParams()
      .set('geometries', 'geojson')
      .set('overview', 'full')
      .set('annotations', 'congestion')
      .set('access_token', this.mapboxToken);

    return this.http.get<MapboxDirectionsResponse>(url, { params }).pipe(
      map((response) => {
        const route = response?.routes?.[0];
        const points = this.mapRouteCoords(route?.geometry?.coordinates);
        if (!points || points.length === 0) {
          return null;
        }
        return {
          points,
          durationSeconds: route?.duration,
          congestion: route?.legs?.[0]?.annotation?.congestion ?? [],
          source: 'mapbox'
        } as RouteResult;
      })
    );
  }

  reverseGeocode(lat: number, lng: number): Observable<ReverseGeocodeResult | null> {
    const params = new HttpParams()
      .set('format', 'json')
      .set('lat', String(lat))
      .set('lon', String(lng))
      .set('zoom', '18')
      .set('addressdetails', '1');

    return this.http.get<NominatimReverseResponse>('https://nominatim.openstreetmap.org/reverse', { params }).pipe(
      map((response) => response?.address ?? null)
    );
  }

  private pickFirstLatLng(results?: NominatimResult[] | null): LatLng | null {
    const first = results?.[0];
    if (!first) {
      return null;
    }
    const lat = Number(first.lat);
    const lng = Number(first.lon);
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
      return null;
    }
    return [lat, lng];
  }

  private mapRouteCoords(coords?: [number, number][] | null): LatLng[] | null {
    if (!coords || coords.length === 0) {
      return null;
    }
    return coords
      .map(([lng, lat]) => [lat, lng] as LatLng)
      .filter(([lat, lng]) => Number.isFinite(lat) && Number.isFinite(lng));
  }
}
