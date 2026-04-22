import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StudentNotificationService, Notification } from '../../core/services/student-notification.service';
import { interval, Subscription } from 'rxjs';
import { Router } from '@angular/router';

@Component({
  selector: 'app-notification-dropdown',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-dropdown.component.html',
  styleUrl: './notification-dropdown.component.css'
})
export class NotificationDropdownComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  unreadCount: number = 0;
  isOpen: boolean = false;
  isJiggling: boolean = false;
  private wsSubscription?: Subscription;

  constructor(
    private notificationService: StudentNotificationService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadNotifications();
    
    // Subscribe to real-time notifications via WebSocket
    this.wsSubscription = this.notificationService.getNewNotifications().subscribe(newNotif => {
      this.notifications.unshift(newNotif);
      this.unreadCount++;
      this.triggerJiggle();
    });
  }

  ngOnDestroy(): void {
    this.wsSubscription?.unsubscribe();
  }

  loadNotifications(): void {
    this.notificationService.getNotifications().subscribe(data => {
      this.notifications = data;
    });
    this.notificationService.getUnreadCount().subscribe(data => {
      this.unreadCount = data.unreadCount;
    });
  }

  toggleDropdown(): void {
    this.isOpen = !this.isOpen;
  }

  markAsRead(id: number, event: Event): void {
    event.stopPropagation();
    this.notificationService.markAsRead(id).subscribe(() => {
      this.loadNotifications();
    });
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe(() => {
      this.loadNotifications();
    });
  }

  triggerJiggle(): void {
    this.isJiggling = true;
    setTimeout(() => {
      this.isJiggling = false;
    }, 1000);
  }

  onNotificationClick(n: Notification): void {
    if (!n.isRead) {
      this.notificationService.markAsRead(n.id).subscribe(() => {
        n.isRead = true;
        this.unreadCount = Math.max(0, this.unreadCount - 1);
      });
    }

    if (n.type === 'CERTIFICATE' && n.referenceId) {
      this.router.navigate(['/trainings', n.referenceId]);
      this.isOpen = false;
    }
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleString();
  }
}
