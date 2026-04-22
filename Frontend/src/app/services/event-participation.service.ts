import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { EventParticipation } from '../models/event-participation.model';
import { environment } from '../environments/environment';
import { ApiHeadersService } from './api-headers.service';

@Injectable({
  providedIn: 'root'
})
export class EventParticipationService {

  private apiUrl = 'http://localhost:8086/api/participations';

  constructor(private http: HttpClient, private headers: ApiHeadersService) {}

  // 🔥 CREATE PARTICIPATION (with full form data)
  participate(
    eventId: number,
    payload: EventParticipation
  ): Observable<EventParticipation> {

    return this.http.post<EventParticipation>(
      `${this.apiUrl}/${eventId}`,
      payload,
      { headers: this.headers.buildHeaders() }
    );
  }

  // 🔥 GET ALL PARTICIPANTS BY EVENT
  getParticipantsByEvent(
    eventId: number
  ): Observable<EventParticipation[]> {

    return this.http.get<any>(
      `${this.apiUrl}/event/${eventId}`,
      { headers: this.headers.buildHeaders() }
    ).pipe(
      map((response) => {
        if (Array.isArray(response)) return response;
        if (Array.isArray(response?.participants)) return response.participants;
        if (Array.isArray(response?.content)) return response.content;
        if (Array.isArray(response?.data)) return response.data;
        return [];
      })
    );
  }

  getPendingRequestsByEvent(
    eventId: number
  ): Observable<EventParticipation[]> {
    return this.http.get<EventParticipation[]>(
      `${this.apiUrl}/requests/pending/event/${eventId}`,
      { headers: this.headers.buildHeaders() }
    );
  }

  getMyRequests(): Observable<EventParticipation[]> {
    return this.http.get<EventParticipation[]>(
      `${this.apiUrl}/requests/me`,
      { headers: this.headers.buildHeaders() }
    );
  }

  approveRequest(
    id: number
  ): Observable<EventParticipation> {
    return this.http.put<EventParticipation>(
      `${this.apiUrl}/requests/${id}/approve`,
      {},
      { headers: this.headers.buildHeaders() }
    );
  }

  rejectRequest(
    id: number
  ): Observable<EventParticipation> {
    return this.http.put<EventParticipation>(
      `${this.apiUrl}/requests/${id}/reject`,
      {},
      { headers: this.headers.buildHeaders() }
    );
  }

  // 🔥 GET ONE PARTICIPANT BY ID
  getParticipationById(
    id: number
  ): Observable<EventParticipation> {

    return this.http.get<EventParticipation>(
      `${this.apiUrl}/${id}`,
      { headers: this.headers.buildHeaders() }
    );
  }

  // 🔥 DELETE PARTICIPATION
  cancelParticipation(
    id: number
  ): Observable<void> {

    return this.http.delete<void>(
      `${this.apiUrl}/${id}`,
      { headers: this.headers.buildHeaders() }
    );
  }
}
