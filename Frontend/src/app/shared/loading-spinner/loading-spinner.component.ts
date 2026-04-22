import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-loading-spinner',
  template: `
    <div class="spinner-container" [class.overlay]="overlay" [class.inline]="!overlay">
      <div class="spinner" [style.width.px]="size" [style.height.px]="size">
        <div class="spinner-ring"></div>
      </div>
      <span *ngIf="text" class="spinner-text">{{ text }}</span>
    </div>
  `,
  styles: [`
    .spinner-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 12px;
    }

    .spinner-container.overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(255, 255, 255, 0.85);
      z-index: 9998;
    }

    .spinner-container.inline {
      padding: 20px;
    }

    .spinner {
      position: relative;
    }

    .spinner-ring {
      width: 100%;
      height: 100%;
      border: 3px solid #e5e7eb;
      border-top-color: #3b82f6;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
      to {
        transform: rotate(360deg);
      }
    }

    .spinner-text {
      color: #4b5563;
      font-size: 14px;
    }
  `]
})
export class LoadingSpinnerComponent {
  @Input() size: number = 40;
  @Input() text: string = '';
  @Input() overlay: boolean = false;
}
