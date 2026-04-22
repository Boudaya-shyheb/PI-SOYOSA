import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { timeout } from 'rxjs/operators';
import { COURSE_API_BASE_URL } from './api.config';
import { ApiHeadersService } from './api-headers.service';
import {
  CourseCreateRequest,
  CourseResponse,
  CourseReviewCreateRequest,
  CourseReviewResponse,
  CourseReviewSummaryResponse,
  CourseUpdateRequest,
  Page
} from '../models/api-models';

@Injectable({ providedIn: 'root' })
export class CourseApiService {
  private readonly baseUrl = `${COURSE_API_BASE_URL}/courses`;

  constructor(private http: HttpClient, private headers: ApiHeadersService) {}

  listCourses(page = 0, size = 50): Observable<Page<CourseResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<CourseResponse>>(this.baseUrl, { params }).pipe(
      timeout(5000)
    );
  }

  getCourse(courseId: string): Observable<CourseResponse> {
    return this.http.get<CourseResponse>(`${this.baseUrl}/${courseId}`).pipe(
      timeout(5000)
    );
  }

  createCourse(request: CourseCreateRequest): Observable<CourseResponse> {
    return this.http.post<CourseResponse>(this.baseUrl, request, { headers: this.headers.buildHeaders() }).pipe(
      timeout(8000)
    );
  }

  updateCourse(courseId: string, request: CourseUpdateRequest): Observable<CourseResponse> {
    return this.http.put<CourseResponse>(`${this.baseUrl}/${courseId}`, request, { headers: this.headers.buildHeaders() }).pipe(
      timeout(8000)
    );
  }

  setActivation(courseId: string, active: boolean): Observable<CourseResponse> {
    const params = new HttpParams().set('active', active);
    return this.http.patch<CourseResponse>(`${this.baseUrl}/${courseId}/activation`, null, {
      params,
      headers: this.headers.buildHeaders()
    });
  }

  deleteCourse(courseId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${courseId}`, { headers: this.headers.buildHeaders() });
  }

  getCourseReviews(courseId: string): Observable<CourseReviewResponse[]> {
    return this.http.get<CourseReviewResponse[]>(`${this.baseUrl}/${courseId}/reviews`, {
      headers: this.headers.buildHeaders()
    }).pipe(timeout(5000));
  }

  getCourseReviewSummary(courseId: string): Observable<CourseReviewSummaryResponse> {
    return this.http.get<CourseReviewSummaryResponse>(`${this.baseUrl}/${courseId}/reviews/summary`).pipe(
      timeout(5000)
    );
  }

  upsertCourseReview(courseId: string, request: CourseReviewCreateRequest): Observable<CourseReviewResponse> {
    return this.http.post<CourseReviewResponse>(`${this.baseUrl}/${courseId}/reviews`, request, {
      headers: this.headers.buildHeaders()
    }).pipe(timeout(7000));
  }
}
