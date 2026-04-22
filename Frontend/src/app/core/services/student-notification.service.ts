import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { WebSocketService } from './web-socket.service';
import { NotificationService as ToastService } from '../../services/notification.service';
import { environment } from '../../environments/environment';

export interface Notification {
  id: number;
  message: string;
  isRead: boolean;
  createdAt: string;
  type?: string;
  referenceId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class StudentNotificationService {
  private apiUrl = `${environment.trainingApiUrl}/notifications`;
  private newNotificationSubject = new Subject<Notification>();

  constructor(
    private http: HttpClient,
    private webSocketService: WebSocketService,
    private toastService: ToastService
  ) {
    this.initWebSocket();
  }

  private initWebSocket(): void {
    this.webSocketService.connect(environment.trainingWebSocketUrl);
    this.webSocketService.subscribeToUserQueue('/queue/notifications', (notification: Notification) => {
      this.newNotificationSubject.next(notification);
      // Trigger a "pop" notification toast
      if (notification.message.includes('certificate')) {
        this.toastService.celebrate('🎉 New Certificate!', notification.message);
      } else {
        this.toastService.info(notification.message);
      }
    });
  }

  getNewNotifications(): Observable<Notification> {
    return this.newNotificationSubject.asObservable();
  }

  getNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(this.apiUrl);
  }

  getUnreadCount(): Observable<{ unreadCount: number }> {
    return this.http.get<{ unreadCount: number }>(`${this.apiUrl}/unread-count`);
  }

  markAsRead(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/read`, null);
  }

  markAllAsRead(): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/read-all`, null);
  }
}
