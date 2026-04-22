import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { NotificationService, Notification } from '../../services/notification.service';

@Component({
  selector: 'app-notification-container',
  template: `
    <div class="notification-container">
      <div
        *ngFor="let notification of notifications; trackBy: trackById"
        class="notification"
        [class.notification-success]="notification.type === 'success'"
        [class.notification-error]="notification.type === 'error'"
        [class.notification-info]="notification.type === 'info'"
        [class.notification-warning]="notification.type === 'warning'"
        [class.notification-celebration]="notification.type === 'celebration'"
      >
        <div class="notification-icon-wrapper">
          <span class="notification-icon">
            <ng-container [ngSwitch]="notification.type">
              <span *ngSwitchCase="'success'">✓</span>
              <span *ngSwitchCase="'error'">✕</span>
              <span *ngSwitchCase="'warning'">⚠</span>
              <span *ngSwitchCase="'celebration'">🎉</span>
              <span *ngSwitchDefault>ℹ</span>
            </ng-container>
          </span>
        </div>
        <div class="notification-content">
          <div *ngIf="notification.title" class="notification-title">{{ notification.title }}</div>
          <div class="notification-message">{{ notification.message }}</div>
          <div *ngIf="notification.showProgress && notification.progress !== undefined" class="notification-progress">
            <div class="notification-progress-bar" [style.width.%]="notification.progress"></div>
          </div>
          <button *ngIf="notification.action" class="notification-action" (click)="executeAction(notification)">
            {{ notification.action.label }}
          </button>
        </div>
        <button class="notification-close" (click)="dismiss(notification.id)">×</button>
        
        <!-- Confetti for celebration -->
        <div *ngIf="notification.type === 'celebration'" class="confetti-container">
          <div *ngFor="let i of confettiPieces" class="confetti" [style.left.%]="i * 10" [style.animation-delay.ms]="i * 100"></div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .notification-container {
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 12px;
      max-width: 420px;
    }

    .notification {
      display: flex;
      align-items: flex-start;
      padding: 16px 20px;
      border-radius: 16px;
      box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15), 0 2px 10px rgba(0, 0, 0, 0.1);
      animation: slideIn 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
      color: white;
      position: relative;
      overflow: hidden;
      backdrop-filter: blur(10px);
    }

    @keyframes slideIn {
      from {
        opacity: 0;
        transform: translateX(100%) scale(0.8);
      }
      to {
        opacity: 1;
        transform: translateX(0) scale(1);
      }
    }

    .notification-success {
      background: linear-gradient(135deg, #2D5757 0%, #1f4444 100%);
      border: 1px solid rgba(201, 168, 76, 0.3);
    }

    .notification-error {
      background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
      border: 1px solid rgba(255, 255, 255, 0.2);
    }

    .notification-info {
      background: linear-gradient(135deg, #2D5757 0%, #1f4444 100%);
      border: 1px solid rgba(201, 168, 76, 0.3);
    }

    .notification-warning {
      background: linear-gradient(135deg, #C9A84C 0%, #b8963f 100%);
      border: 1px solid rgba(255, 255, 255, 0.2);
    }

    .notification-celebration {
      background: linear-gradient(135deg, #2D5757 0%, #1f4444 50%, #C9A84C 100%);
      border: 2px solid #C9A84C;
      animation: slideIn 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55), celebrate 0.5s ease-in-out 0.4s;
    }

    @keyframes celebrate {
      0%, 100% { transform: scale(1); }
      50% { transform: scale(1.02); }
    }

    .notification-icon-wrapper {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 14px;
      flex-shrink: 0;
    }

    .notification-icon {
      font-size: 18px;
      font-weight: bold;
    }

    .notification-content {
      flex: 1;
      min-width: 0;
    }

    .notification-title {
      font-size: 15px;
      font-weight: 700;
      margin-bottom: 4px;
      line-height: 1.3;
    }

    .notification-message {
      font-size: 14px;
      line-height: 1.5;
      opacity: 0.95;
    }

    .notification-progress {
      margin-top: 10px;
      height: 4px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 2px;
      overflow: hidden;
    }

    .notification-progress-bar {
      height: 100%;
      background: #C9A84C;
      border-radius: 2px;
      transition: width 0.3s ease;
    }

    .notification-action {
      margin-top: 10px;
      padding: 6px 14px;
      background: rgba(255, 255, 255, 0.2);
      border: 1px solid rgba(255, 255, 255, 0.3);
      border-radius: 8px;
      color: white;
      font-size: 13px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
    }

    .notification-action:hover {
      background: rgba(255, 255, 255, 0.3);
    }

    .notification-close {
      background: none;
      border: none;
      color: white;
      font-size: 20px;
      cursor: pointer;
      padding: 0;
      margin-left: 12px;
      opacity: 0.6;
      transition: all 0.2s;
      width: 24px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
    }

    .notification-close:hover {
      opacity: 1;
      background: rgba(255, 255, 255, 0.2);
    }

    /* Confetti */
    .confetti-container {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      pointer-events: none;
      overflow: hidden;
    }

    .confetti {
      position: absolute;
      width: 8px;
      height: 8px;
      top: -10px;
      animation: confettiFall 2s ease-in forwards;
    }

    .confetti:nth-child(odd) {
      background: #C9A84C;
      border-radius: 50%;
    }

    .confetti:nth-child(even) {
      background: #fff;
      transform: rotate(45deg);
    }

    @keyframes confettiFall {
      0% {
        top: -10px;
        opacity: 1;
        transform: rotate(0deg) translateX(0);
      }
      100% {
        top: 100%;
        opacity: 0;
        transform: rotate(720deg) translateX(20px);
      }
    }

    @media (max-width: 480px) {
      .notification-container {
        left: 10px;
        right: 10px;
        max-width: none;
      }
    }
  `]
})
export class NotificationContainerComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  confettiPieces = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
  private subscription?: Subscription;

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.subscription = this.notificationService.notifications$.subscribe(
      notifications => this.notifications = notifications
    );
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

  trackById(index: number, notification: Notification): string {
    return notification.id;
  }

  dismiss(id: string): void {
    this.notificationService.dismiss(id);
  }

  executeAction(notification: Notification): void {
    if (notification.action) {
      notification.action.callback();
      this.dismiss(notification.id);
    }
  }
}
