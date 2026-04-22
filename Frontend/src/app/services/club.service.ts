import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Club } from '../models/club.model';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ClubService {

  private readonly apiUrl = 'http://localhost:8086/api/clubs';

  constructor(private http: HttpClient) {}

  // ==========================
  // GET ALL CLUBS
  // ==========================
  getAll(): Observable<Club[]> {
    return this.http.get<Club[]>(this.apiUrl)
      .pipe(catchError(this.handleError));
  }

  // ==========================
  // GET CLUB BY ID
  // ==========================
  getById(id: number): Observable<Club> {
    return this.http.get<Club>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  // ==========================
  // CREATE CLUB
  // ==========================
  create(club: Club): Observable<Club> {
    return this.http.post<Club>(this.apiUrl, club)
      .pipe(catchError(this.handleError));
  }

  // ==========================
  // UPDATE CLUB
  // ==========================
  update(id: number, club: Club): Observable<Club> {
    return this.http.put<Club>(`${this.apiUrl}/${id}`, club)
      .pipe(catchError(this.handleError));
  }

  // ==========================
  // DELETE CLUB
  // ==========================
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  // ==========================
  // ERROR HANDLER
  // ==========================
  private handleError(error: HttpErrorResponse) {
    console.error('API Error:', error);

    let errorMessage = 'An unknown error occurred!';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Client Error: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage = `Server Error: ${error.status} - ${error.message}`;
    }

    return throwError(() => new Error(errorMessage));
  }
}
