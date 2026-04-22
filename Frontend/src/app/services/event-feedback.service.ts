import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { EventFeedback } from '../models/event-feedback.model';

@Injectable({
  providedIn: 'root'
})
export class EventFeedbackService {

  private apiUrl = 'http://localhost:8086/api/feedbacks';

  constructor(private http: HttpClient) {}

  // 🔥 Add feedback
  addFeedback(eventId: number, feedback: EventFeedback): Observable<EventFeedback> {
    return this.http.post<EventFeedback>(
      `${this.apiUrl}/${eventId}`,
      feedback
    );
  }

  // 🔥 Get feedbacks by event
  getFeedbacksByEvent(eventId: number): Observable<EventFeedback[]> {
    return this.http.get<EventFeedback[]>(
      `${this.apiUrl}/event/${eventId}`
    );
  }

  // 🔥 Delete feedback
  deleteFeedback(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${id}`
    );
  }
}
