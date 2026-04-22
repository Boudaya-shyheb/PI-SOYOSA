import { Injectable } from '@angular/core';
import { Client, Message } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';
import { filter, first } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client!: Client;
  private state = new BehaviorSubject<WebSocketState>(WebSocketState.DISCONNECTED);

  constructor(private authService: AuthService) {
    this.initClient(environment.webSocketUrl);
  }

  private initClient(url: string): void {
    if (this.client) {
      this.client.deactivate();
    }
    this.client = new Client({
      webSocketFactory: () => {
        const socket = SockJS as any;
        return socket.default ? new socket.default(url) : new socket(url);
      },
      debug: (msg) => console.log('STOMP: ' + msg),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = () => {
      this.state.next(WebSocketState.CONNECTED);
    };

    this.client.onDisconnect = () => {
      this.state.next(WebSocketState.DISCONNECTED);
    };

    this.client.onStompError = (frame) => {
      console.error('STOMP Error:', frame.headers['message']);
      this.state.next(WebSocketState.ERROR);
    };
  }

  connect(url?: string): void {
    if (url && url !== this.client.brokerURL) {
      this.initClient(url);
    }
    if (this.state.value !== WebSocketState.CONNECTED) {
      this.client.activate();
    }
  }

  disconnect(): void {
    if (this.state.value === WebSocketState.CONNECTED) {
      this.client.deactivate();
    }
  }

  subscribe(topic: string, callback: (payload: any) => void): void {
    this.state.pipe(
      filter(s => s === WebSocketState.CONNECTED),
      first()
    ).subscribe(() => {
      this.client.subscribe(topic, (message: Message) => {
        callback(JSON.parse(message.body));
      });
    });
  }

  subscribeToUserQueue(queue: string, callback: (payload: any) => void): void {
    // Standard Spring STOMP user-specific subscription
    this.subscribe(`/user${queue}`, callback);
  }
}

export enum WebSocketState {
  CONNECTED,
  DISCONNECTED,
  ERROR
}
