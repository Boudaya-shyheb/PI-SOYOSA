import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClubJoinRequest, ClubMember } from '../models/member.model';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ClubMemberService {

  private readonly apiUrl = `http://localhost:8086/api/club-members`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ClubMember[]> {
    return this.http.get<ClubMember[]>(this.apiUrl);
  }

  getById(id: number): Observable<ClubMember> {
    return this.http.get<ClubMember>(`${this.apiUrl}/${id}`);
  }

  create(member: ClubMember): Observable<ClubMember> {
    return this.http.post<ClubMember>(this.apiUrl, member);
  }

  joinClub(clubId: number): Observable<ClubJoinRequest> {
    return this.http.post<ClubJoinRequest>(`${this.apiUrl}/join/${clubId}`, {});
  }

  getPendingRequestsByClub(clubId: number): Observable<ClubJoinRequest[]> {
    return this.http.get<ClubJoinRequest[]>(`${this.apiUrl}/requests/pending/${clubId}`);
  }

  getMyJoinRequests(): Observable<ClubJoinRequest[]> {
    return this.http.get<ClubJoinRequest[]>(`${this.apiUrl}/requests/me`);
  }

  approveJoinRequest(requestId: number): Observable<ClubJoinRequest> {
    return this.http.put<ClubJoinRequest>(`${this.apiUrl}/requests/${requestId}/approve`, {});
  }

  rejectJoinRequest(requestId: number): Observable<ClubJoinRequest> {
    return this.http.put<ClubJoinRequest>(`${this.apiUrl}/requests/${requestId}/reject`, {});
  }

  update(id: number, member: ClubMember): Observable<ClubMember> {
    return this.http.put<ClubMember>(`${this.apiUrl}/${id}`, member);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getJoinQuiz(clubId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/join-quiz/${clubId}`);
  }

  evaluateJoinQuiz(clubId: number, answers: number[], questionIds: number[], email?: string): Observable<{message: string}> {
    const url = `${this.apiUrl}/join-quiz/${clubId}/evaluate${email ? '?email=' + encodeURIComponent(email) : ''}`;
    return this.http.post<{message: string}>(url, { answers, questionIds });
  }

  getQuizPassingScore(): Observable<{ passingScore: number }> {
    return this.http.get<{ passingScore: number }>(`${this.apiUrl}/join-quiz/passing-score`);
  }

  updateQuizPassingScore(passingScore: number): Observable<{ passingScore: number }> {
    return this.http.put<{ passingScore: number }>(`${this.apiUrl}/join-quiz/passing-score`, { passingScore });
  }
}
