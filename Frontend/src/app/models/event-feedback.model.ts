export interface EventFeedback {
  id?: number;
  rating: number;
  comment: string;
  submittedAt?: string;
  eventId: number;
}