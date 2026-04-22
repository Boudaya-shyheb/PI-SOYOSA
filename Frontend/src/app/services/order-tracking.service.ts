import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { Client } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import { environment } from 'src/app/environments/environment';
import { AuthService } from './auth.service';

export interface LocationUpdate {
  orderId: number;
  courierId?: number | null;
  lat: number;
  lng: number;
  speed?: number | null;
  heading?: number | null;
  timestamp?: string | null;
}

export type TrackingStatus = 'disconnected' | 'connecting' | 'connected' | 'error';

@Injectable({
  providedIn: 'root'
})
export class OrderTrackingService {
  private client: Client | null = null;
  private updatesSubject = new Subject<LocationUpdate>();
  private statusSubject = new BehaviorSubject<TrackingStatus>('disconnected');

  constructor(private auth: AuthService) {}

  get status$(): Observable<TrackingStatus> {
    return this.statusSubject.asObservable();
  }

  connect(orderId: number): Observable<LocationUpdate> {
    this.disconnect();
    this.statusSubject.next('connecting');

    const token = this.auth.getToken();
    const wsUrl = token
      ? `${environment.trackingWebSocketUrl}?token=${encodeURIComponent(token)}`
      : environment.trackingWebSocketUrl;

    const client = new Client({
      webSocketFactory: () => new SockJS(wsUrl) as any,
      reconnectDelay: 5000
    });

    client.onConnect = () => {
      this.statusSubject.next('connected');
      client.subscribe(`/topic/orders/${orderId}`, (message) => {
        try {
          const payload = JSON.parse(message.body) as LocationUpdate;
          this.updatesSubject.next(payload);
        } catch (error) {
          this.statusSubject.next('error');
        }
      });
    };

    client.onStompError = () => {
      this.statusSubject.next('error');
    };

    client.onWebSocketClose = () => {
      this.statusSubject.next('disconnected');
    };

    client.activate();
    this.client = client;

    return this.updatesSubject.asObservable();
  }

  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this.statusSubject.next('disconnected');
  }
}
