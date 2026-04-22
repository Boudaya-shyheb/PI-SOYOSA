import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';
import { COURSE_API_BASE_URL } from './api.config';
import { ApiHeadersService } from './api-headers.service';
import { EnrollmentRequest, EnrollmentResponse, LessonCompletionRequest, ProgressResponse, PaymentCheckoutResponse } from '../models/api-models';

@Injectable({ providedIn: 'root' })
export class EnrollmentApiService {
  private readonly baseUrl = `${COURSE_API_BASE_URL}/enrollments`;

  constructor(private http: HttpClient, private headers: ApiHeadersService) {}

  requestEnrollment(courseId: string): Observable<EnrollmentResponse> {
    const request: EnrollmentRequest = { courseId };
    return this.http.post<EnrollmentResponse>(this.baseUrl, request, { headers: this.headers.buildHeaders() }).pipe(
      timeout(8000)
    );
  }

  getEnrollment(courseId: string): Observable<EnrollmentResponse | null> {
    return this.http.get<EnrollmentResponse>(`${this.baseUrl}/${courseId}`, { headers: this.headers.buildHeaders() }).pipe(
      timeout(3000),
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404) {
          return of(null);
        }
        return throwError(() => error);
      })
    );
  }

  getEnrollmentById(enrollmentId: string): Observable<EnrollmentResponse> {
    return this.http.get<EnrollmentResponse>(`${this.baseUrl}/id/${enrollmentId}`, { headers: this.headers.buildHeaders() }).pipe(
      timeout(5000)
    );
  }

  completeLesson(courseId: string, lessonId: string): Observable<ProgressResponse> {
    const request: LessonCompletionRequest = { lessonId };
    return this.http.post<ProgressResponse>(`${this.baseUrl}/${courseId}/lessons/complete`, request, { headers: this.headers.buildHeaders() }).pipe(
      timeout(8000)
    );
  }

  getProgress(courseId: string): Observable<ProgressResponse> {
    return this.http.get<ProgressResponse>(`${this.baseUrl}/${courseId}/progress`, { headers: this.headers.buildHeaders() }).pipe(
      timeout(5000)
    );
  }

  getNextLesson(courseId: string): Observable<string> {
    return this.http.get<string>(`${this.baseUrl}/${courseId}/next-lesson`, { headers: this.headers.buildHeaders() }).pipe(
      timeout(5000)
    );
  }

  checkoutPaymentSuccess(courseId: string): Observable<EnrollmentResponse> {
    return this.http.post<EnrollmentResponse>(`${this.baseUrl}/${courseId}/payment-success`, {}, { headers: this.headers.buildHeaders() }).pipe(
      timeout(8000)
    );
  }

  createStripeCheckout(courseId: string): Observable<PaymentCheckoutResponse> {
    const request = { courseId };
    return this.http.post<PaymentCheckoutResponse>(`${COURSE_API_BASE_URL}/payments/checkout`, request, { headers: this.headers.buildHeaders() }).pipe(
      timeout(8000)
    );
  }

  confirmPayment(sessionId: string): Observable<string> {
    return this.http.post(`${COURSE_API_BASE_URL}/payments/confirm/${sessionId}`, {}, { 
      headers: this.headers.buildHeaders(),
      responseType: 'text' 
    }).pipe(
      timeout(10000)
    );
  }
}
