import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClubActivity } from '../models/activity.model';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ClubActivityService {

  private readonly apiUrl = 'http://localhost:8086/api/club-activities';

  constructor(private http: HttpClient) { }

  getAll(): Observable<ClubActivity[]> {
    return this.http.get<ClubActivity[]>(this.apiUrl);
  }

  getById(id: number): Observable<ClubActivity> {
    return this.http.get<ClubActivity>(`${this.apiUrl}/${id}`);
  }

  getByClubId(clubId: number): Observable<ClubActivity[]> {
    return this.http.get<ClubActivity[]>(`${this.apiUrl}/club/${clubId}`);
  }

  create(activity: ClubActivity): Observable<ClubActivity> {
    return this.http.post<ClubActivity>(this.apiUrl, activity);
  }

  update(id: number, activity: ClubActivity): Observable<ClubActivity> {
    return this.http.put<ClubActivity>(`${this.apiUrl}/${id}`, activity);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
