import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, catchError } from 'rxjs';
import { map } from 'rxjs/operators';
import { COURSE_API_BASE_URL } from './api.config';
import { ApiHeadersService } from './api-headers.service';
import { Quiz, QuizQuestion, QuestionType } from '../models/quiz-models';

export interface QuizResponse extends Quiz {
  // The API response might have slight differences from the main model
}

export interface QuizCreateRequest extends Partial<Quiz> {
  // Request-specific fields
}

export interface QuizUpdateRequest extends Partial<Quiz> {
  // Request-specific fields
}

@Injectable({ providedIn: 'root' })
export class QuizApiService {
  private readonly baseUrl = `${COURSE_API_BASE_URL}/quizzes`;

  constructor(private http: HttpClient, private headers: ApiHeadersService) {}

  createQuiz(request: QuizCreateRequest): Observable<QuizResponse> {
    return this.http.post<QuizResponse>(this.baseUrl, request, { headers: this.headers.buildHeaders() });
  }

  updateQuiz(quizId: string, request: QuizUpdateRequest): Observable<QuizResponse> {
    return this.http.put<QuizResponse>(`${this.baseUrl}/${quizId}`, request, { headers: this.headers.buildHeaders() });
  }

  deleteQuiz(quizId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${quizId}`, { headers: this.headers.buildHeaders() });
  }

  getQuiz(quizId: string): Observable<QuizResponse> {
    return this.http.get<QuizResponse>(`${this.baseUrl}/${quizId}`);
  }

  getQuizByLesson(lessonId: string): Observable<QuizResponse | null> {
    return this.http.get<QuizResponse>(`${this.baseUrl}/lesson/${lessonId}`).pipe(
      catchError(() => of(null))
    );
  }

  getQuizzesByCourse(courseId: string): Observable<QuizResponse[]> {
    return this.http.get<QuizResponse[]>(`${this.baseUrl}/course/${courseId}`);
  }

  /**
   * Check if a quiz exists for a lesson.
   */
  hasQuiz(lessonId: string): Observable<boolean> {
    return this.getQuizByLesson(lessonId).pipe(
      map(quiz => quiz !== null)
    );
  }
}
