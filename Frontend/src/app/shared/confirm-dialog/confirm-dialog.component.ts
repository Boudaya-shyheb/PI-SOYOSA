import { Component, Input, Output, EventEmitter } from '@angular/core';

export type ConfirmDialogType = 'danger' | 'warning' | 'info' | 'success';

@Component({
  selector: 'app-confirm-dialog',
  template: `
    <div *ngIf="isOpen" class="dialog-backdrop" (click)="onBackdropClick($event)">
      <div class="dialog-container" [class]="'dialog-' + type">
        <div class="dialog-icon">
          <ng-container [ngSwitch]="type">
            <svg *ngSwitchCase="'danger'" width="32" height="32" viewBox="0 0 24 24" fill="none">
              <path d="M12 9V13M12 17H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <svg *ngSwitchCase="'warning'" width="32" height="32" viewBox="0 0 24 24" fill="none">
              <path d="M12 9V13M12 17H12.01M10.29 3.86L1.82 18A2 2 0 003.41 21H20.59A2 2 0 0022.18 18L13.71 3.86A2 2 0 0010.29 3.86Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <svg *ngSwitchCase="'success'" width="32" height="32" viewBox="0 0 24 24" fill="none">
              <path d="M9 12L11 14L15 10M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <svg *ngSwitchDefault width="32" height="32" viewBox="0 0 24 24" fill="none">
              <path d="M12 16V12M12 8H12.01M22 12C22 17.5228 17.5228 22 12 22C6.47715 22 2 17.5228 2 12C2 6.47715 6.47715 2 12 2C17.5228 2 22 6.47715 22 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </ng-container>
        </div>
        
        <h3 class="dialog-title">{{ title }}</h3>
        <p class="dialog-message">{{ message }}</p>
        
        <div class="dialog-actions">
          <button class="dialog-btn cancel" (click)="onCancel()" [disabled]="loading">
            {{ cancelText }}
          </button>
          <button class="dialog-btn confirm" [class]="'btn-' + type" (click)="onConfirm()" [disabled]="loading">
            <span *ngIf="loading" class="btn-spinner"></span>
            {{ loading ? loadingText : confirmText }}
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dialog-backdrop {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.6);
      backdrop-filter: blur(4px);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1100;
      padding: 1rem;
      animation: fadeIn 0.2s ease;
    }

    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }

    .dialog-container {
      background: #fff;
      border-radius: 20px;
      padding: 32px;
      max-width: 400px;
      width: 100%;
      text-align: center;
      animation: scaleIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
    }

    @keyframes scaleIn {
      from {
        opacity: 0;
        transform: scale(0.9);
      }
      to {
        opacity: 1;
        transform: scale(1);
      }
    }

    .dialog-icon {
      width: 72px;
      height: 72px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 20px;
    }

    .dialog-danger .dialog-icon {
      background: linear-gradient(135deg, rgba(220, 38, 38, 0.1), rgba(185, 28, 28, 0.1));
      color: #dc2626;
    }

    .dialog-warning .dialog-icon {
      background: linear-gradient(135deg, rgba(201, 168, 76, 0.1), rgba(184, 150, 63, 0.1));
      color: #C9A84C;
    }

    .dialog-success .dialog-icon {
      background: linear-gradient(135deg, rgba(45, 87, 87, 0.1), rgba(31, 68, 68, 0.1));
      color: #2D5757;
    }

    .dialog-info .dialog-icon {
      background: linear-gradient(135deg, rgba(45, 87, 87, 0.1), rgba(31, 68, 68, 0.1));
      color: #2D5757;
    }

    .dialog-title {
      font-size: 20px;
      font-weight: 700;
      color: #222;
      margin: 0 0 12px 0;
    }

    .dialog-message {
      font-size: 14px;
      color: #666;
      line-height: 1.6;
      margin: 0 0 24px 0;
    }

    .dialog-actions {
      display: flex;
      gap: 12px;
    }

    .dialog-btn {
      flex: 1;
      padding: 14px 20px;
      border-radius: 12px;
      font-size: 15px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
      border: none;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
    }

    .dialog-btn.cancel {
      background: #f0f0f0;
      color: #666;
    }

    .dialog-btn.cancel:hover {
      background: #e0e0e0;
    }

    .dialog-btn.confirm {
      color: #fff;
    }

    .dialog-btn.btn-danger {
      background: linear-gradient(135deg, #dc2626, #b91c1c);
    }

    .dialog-btn.btn-danger:hover {
      box-shadow: 0 6px 20px rgba(220, 38, 38, 0.3);
    }

    .dialog-btn.btn-warning {
      background: linear-gradient(135deg, #C9A84C, #b8963f);
    }

    .dialog-btn.btn-warning:hover {
      box-shadow: 0 6px 20px rgba(201, 168, 76, 0.3);
    }

    .dialog-btn.btn-success,
    .dialog-btn.btn-info {
      background: linear-gradient(135deg, #2D5757, #1f4444);
    }

    .dialog-btn.btn-success:hover,
    .dialog-btn.btn-info:hover {
      box-shadow: 0 6px 20px rgba(45, 87, 87, 0.3);
    }

    .dialog-btn:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }

    .btn-spinner {
      width: 16px;
      height: 16px;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-top-color: #fff;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class ConfirmDialogComponent {
  @Input() isOpen = false;
  @Input() type: ConfirmDialogType = 'info';
  @Input() title = 'Confirm Action';
  @Input() message = 'Are you sure you want to proceed?';
  @Input() confirmText = 'Confirm';
  @Input() cancelText = 'Cancel';
  @Input() loading = false;
  @Input() loadingText = 'Processing...';

  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('dialog-backdrop') && !this.loading) {
      this.onCancel();
    }
  }

  onConfirm(): void {
    this.confirm.emit();
  }

  onCancel(): void {
    if (!this.loading) {
      this.cancel.emit();
    }
  }
}
