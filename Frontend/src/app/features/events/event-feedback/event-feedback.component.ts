import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EventFeedbackService } from '../../../services/event-feedback.service';
import { EventFeedback } from '../../../models/event-feedback.model';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-event-feedback',
  standalone: true,
  templateUrl: './event-feedback.component.html',
  styleUrls: ['./event-feedback.component.css'],
  imports: [CommonModule, FormsModule]
})
export class EventFeedbackComponent implements OnInit {

  @Input() eventId!: number;

  feedbacks: EventFeedback[] = [];
  sortOption: 'latest' | 'highest' | 'lowest' = 'latest';
  hoveredRating: number | null = null;
  readonly starValues = [1, 2, 3, 4, 5];
  readonly distributionRatings = [5, 4, 3, 2, 1];

  form: EventFeedback = {
    rating: 0,
    comment: '',
    eventId: 0
  };

  loading = false;
  errorMessage = '';

  constructor(
    private feedbackService: EventFeedbackService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.form.eventId = this.eventId;
    this.loadFeedbacks();
  }

  loadFeedbacks(): void {
    this.feedbackService.getFeedbacksByEvent(this.eventId)
      .subscribe({
        next: (data) => this.feedbacks = data ?? [],
        error: () => this.errorMessage = 'Failed to load feedbacks'
      });
  }

  submit(): void {

    if (this.form.rating < 1 || this.form.rating > 5) {
      this.errorMessage = 'Rating must be between 1 and 5';
      return;
    }

    this.loading = true;
    this.form.eventId = this.eventId;

    this.feedbackService.addFeedback(this.eventId, this.form)
      .subscribe({
        next: () => {
          this.loading = false;
          this.form.rating = 0;
          this.form.comment = '';
          this.hoveredRating = null;
          this.loadFeedbacks();
        },
        error: () => {
          this.loading = false;
          this.errorMessage = 'Failed to submit feedback';
        }
      });
  }

  deleteFeedback(id: number): void {
    this.feedbackService.deleteFeedback(id)
      .subscribe(() => this.loadFeedbacks());
  }

  setRating(value: number): void {
    this.form.rating = value;
  }

  setHover(value: number | null): void {
    this.hoveredRating = value;
  }

  get displayRating(): number {
    return this.hoveredRating ?? this.form.rating;
  }

  isStarActive(value: number): boolean {
    return value <= this.displayRating;
  }

  get totalReviews(): number {
    return this.feedbacks.length;
  }

  get averageRating(): number {
    if (this.feedbacks.length === 0) {
      return 0;
    }
    const total = this.feedbacks.reduce((sum, item) => sum + (item.rating || 0), 0);
    return total / this.feedbacks.length;
  }

  get sortedFeedbacks(): EventFeedback[] {
    const list = [...this.feedbacks];
    if (this.sortOption === 'highest') {
      return list.sort((a, b) => (b.rating || 0) - (a.rating || 0));
    }
    if (this.sortOption === 'lowest') {
      return list.sort((a, b) => (a.rating || 0) - (b.rating || 0));
    }
    return list;
  }

  ratingCount(rating: number): number {
    return this.feedbacks.filter(item => item.rating === rating).length;
  }

  ratingWidth(rating: number): string {
    const total = this.totalReviews;
    if (total === 0) {
      return '0%';
    }
    const count = this.ratingCount(rating);
    return `${(count / total) * 100}%`;
  }
}
