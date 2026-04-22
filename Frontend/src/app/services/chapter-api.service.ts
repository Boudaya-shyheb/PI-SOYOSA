import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { COURSE_API_BASE_URL } from './api.config';
import { ApiHeadersService } from './api-headers.service';
import { ChapterCreateRequest, ChapterResponse, ChapterUpdateRequest } from '../models/api-models';

@Injectable({ providedIn: 'root' })
export class ChapterApiService {
  private readonly baseUrl = COURSE_API_BASE_URL;

  constructor(private http: HttpClient, private headers: ApiHeadersService) {}

  createChapter(request: ChapterCreateRequest): Observable<ChapterResponse> {
    return this.http.post<ChapterResponse>(`${this.baseUrl}/chapters`, request, { headers: this.headers.buildHeaders() });
  }

  updateChapter(chapterId: string, request: ChapterUpdateRequest): Observable<ChapterResponse> {
    return this.http.put<ChapterResponse>(`${this.baseUrl}/chapters/${chapterId}`, request, { headers: this.headers.buildHeaders() });
  }

  getChapter(chapterId: string): Observable<ChapterResponse> {
    return this.http.get<ChapterResponse>(`${this.baseUrl}/chapters/${chapterId}`);
  }

  listChapters(courseId: string): Observable<ChapterResponse[]> {
    return this.http.get<ChapterResponse[]>(`${this.baseUrl}/courses/${courseId}/chapters`);
  }

  deleteChapter(chapterId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/chapters/${chapterId}`, { headers: this.headers.buildHeaders() });
  }
}
