import { Component, Input, Output, EventEmitter } from '@angular/core';

export interface EnrollmentCourse {
  id: string;
  title: string;
  description: string;
  level: string;
  image?: string;
  totalChapters?: number;
  totalLessons?: number;
  isPaid?: boolean;
  price?: number;
}

@Component({
  selector: 'app-enrollment-modal',
  templateUrl: './enrollment-modal.component.html',
  styleUrls: ['./enrollment-modal.component.css']
})
export class EnrollmentModalComponent {
  @Input() course: EnrollmentCourse | null = null;
  @Input() isOpen = false;
  @Input() enrolling = false;
  @Input() enrolled = false;

  @Output() close = new EventEmitter<void>();
  @Output() enroll = new EventEmitter<string>();
  @Output() startLearning = new EventEmitter<string>();

  currentStep: 'preview' | 'confirm' | 'success' = 'preview';

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-backdrop')) {
      this.onClose();
    }
  }

  onClose(): void {
    this.currentStep = 'preview';
    this.close.emit();
  }

  onProceed(): void {
    if (this.currentStep === 'preview') {
      this.currentStep = 'confirm';
    }
  }

  onConfirmEnroll(): void {
    if (this.course) {
      this.enroll.emit(this.course.id);
    }
  }

  onStartLearning(): void {
    if (this.course) {
      this.startLearning.emit(this.course.id);
      this.onClose();
    }
  }

  setSuccess(): void {
    this.currentStep = 'success';
  }

  goBack(): void {
    if (this.currentStep === 'confirm') {
      this.currentStep = 'preview';
    }
  }

  get levelLabel(): string {
    const levels: Record<string, string> = {
      'A1': 'Beginner',
      'A2': 'Elementary',
      'B1': 'Intermediate',
      'B2': 'Upper Intermediate',
      'C1': 'Advanced',
      'C2': 'Proficiency'
    };
    return levels[this.course?.level || ''] || this.course?.level || '';
  }
}
