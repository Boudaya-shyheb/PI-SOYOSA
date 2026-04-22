import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export type NotificationType = 'success' | 'error' | 'info' | 'warning' | 'celebration';

export interface NotificationAction {
  label: string;
  callback: () => void;
}

export interface Notification {
  id: string;
  type: NotificationType;
  title?: string;
  message: string;
  duration?: number;
  icon?: string;
  action?: NotificationAction;
  progress?: number;
  showProgress?: boolean;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  
  notifications$: Observable<Notification[]> = this.notificationsSubject.asObservable();

  show(message: string, type: NotificationType = 'info', duration: number = 4000): void {
    const notification: Notification = {
      id: this.generateId(),
      type,
      message,
      duration
    };

    const current = this.notificationsSubject.value;
    this.notificationsSubject.next([...current, notification]);

    if (duration > 0) {
      setTimeout(() => this.dismiss(notification.id), duration);
    }
  }

  showAdvanced(options: Partial<Notification> & { message: string }): string {
    const notification: Notification = {
      id: this.generateId(),
      type: options.type || 'info',
      message: options.message,
      title: options.title,
      icon: options.icon,
      action: options.action,
      duration: options.duration ?? 4000,
      progress: options.progress,
      showProgress: options.showProgress
    };

    const current = this.notificationsSubject.value;
    this.notificationsSubject.next([...current, notification]);

    if (notification.duration && notification.duration > 0) {
      setTimeout(() => this.dismiss(notification.id), notification.duration);
    }

    return notification.id;
  }

  updateProgress(id: string, progress: number): void {
    const current = this.notificationsSubject.value;
    const updated = current.map(n => 
      n.id === id ? { ...n, progress: Math.min(100, Math.max(0, progress)) } : n
    );
    this.notificationsSubject.next(updated);
  }

  success(message: string, duration: number = 4000): void {
    this.show(message, 'success', duration);
  }

  successWithTitle(title: string, message: string, duration: number = 4000): void {
    this.showAdvanced({ type: 'success', title, message, duration });
  }

  error(message: string, duration: number = 5000): void {
    this.show(message, 'error', duration);
  }

  errorWithTitle(title: string, message: string, duration: number = 5000): void {
    this.showAdvanced({ type: 'error', title, message, duration });
  }

  info(message: string, duration: number = 4000): void {
    this.show(message, 'info', duration);
  }

  warning(message: string, duration: number = 4000): void {
    this.show(message, 'warning', duration);
  }

  celebrate(title: string, message: string, duration: number = 6000): void {
    this.showAdvanced({ type: 'celebration', title, message, duration });
  }

  enrollmentSuccess(courseName: string): void {
    this.showAdvanced({
      type: 'celebration',
      title: '🎉 Successfully Enrolled!',
      message: `Welcome to "${courseName}"! Your learning journey begins now.`,
      duration: 5000
    });
  }

  quizPassed(score: number, passingScore: number): void {
    this.showAdvanced({
      type: 'celebration',
      title: '🏆 Congratulations!',
      message: `You passed with ${score}%! (Required: ${passingScore}%)`,
      duration: 6000
    });
  }

  quizFailed(score: number, passingScore: number): void {
    this.showAdvanced({
      type: 'warning',
      title: '📝 Keep Practicing',
      message: `Score: ${score}% (Need ${passingScore}% to pass). You can try again!`,
      duration: 5000
    });
  }

  dismiss(id: string): void {
    const current = this.notificationsSubject.value;
    this.notificationsSubject.next(current.filter(n => n.id !== id));
  }

  dismissAll(): void {
    this.notificationsSubject.next([]);
  }

  private generateId(): string {
    return `notif-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  }
}
