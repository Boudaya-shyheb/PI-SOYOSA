import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { COURSE_API_BASE_URL } from './api.config';
import { ApiHeadersService } from './api-headers.service';
import { LessonCreateRequest, LessonResponse, LessonUpdateRequest } from '../models/api-models';

@Injectable({ providedIn: 'root' })
export class LessonApiService {
  private readonly baseUrl = COURSE_API_BASE_URL;

  constructor(private http: HttpClient, private headers: ApiHeadersService) {}

  createLesson(request: LessonCreateRequest): Observable<LessonResponse> {
    return this.http.post<LessonResponse>(`${this.baseUrl}/lessons`, request, { headers: this.headers.buildHeaders() });
  }

  updateLesson(lessonId: string, request: LessonUpdateRequest): Observable<LessonResponse> {
    return this.http.put<LessonResponse>(`${this.baseUrl}/lessons/${lessonId}`, request, { headers: this.headers.buildHeaders() });
  }

  getLesson(lessonId: string): Observable<LessonResponse> {
    return this.http.get<LessonResponse>(`${this.baseUrl}/lessons/${lessonId}`);
  }

  listLessonsByChapter(chapterId: string): Observable<LessonResponse[]> {
    return this.http.get<LessonResponse[]>(`${this.baseUrl}/chapters/${chapterId}/lessons`);
  }

  listLessonsByCourse(courseId: string): Observable<LessonResponse[]> {
    return this.http.get<LessonResponse[]>(`${this.baseUrl}/courses/${courseId}/lessons`);
  }

  deleteLesson(lessonId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/lessons/${lessonId}`, { headers: this.headers.buildHeaders() });
  }
}
