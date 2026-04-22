import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { map } from 'rxjs/operators';
import { QuizAttempt } from '../models/assessment-models';
import { COURSE_API_BASE_URL } from './api.config';
import { ApiHeadersService } from './api-headers.service';

interface QuizSubmissionResponse {
  id: string;
  courseId: string;
  lessonId?: string;
  quizId: string;
  studentId: string;
  answers: any[];
  score: number | null;
  passingScore?: number | null;
  passed?: boolean | null;
  submittedAt: string;
  gradedAt?: string | null;
  gradedBy?: string | null;
}

interface QuizSubmissionGradeRequest {
  score: number;
}

@Injectable({ providedIn: 'root' })
export class QuizAttemptService {
  private readonly baseUrl = COURSE_API_BASE_URL;

  constructor(private http: HttpClient, private headers: ApiHeadersService) {}

  getAttempts(courseId: string): Observable<QuizAttempt[]> {
    return this.getMyResults().pipe(
      map(attempts => attempts.filter(attempt => attempt.courseId === courseId))
    );
  }

  getAttemptsByLesson(lessonId: string): Observable<QuizAttempt[]> {
    return this.getMySubmission(lessonId).pipe(
      map(submission => submission ? [submission] : [])
    );
  }

  getMySubmission(lessonId: string): Observable<QuizAttempt | null> {
    return this.http.get<QuizSubmissionResponse>(
      `${this.baseUrl}/quiz/${lessonId}/my-submission`,
      { headers: this.headers.buildHeaders() }
    ).pipe(
      map(response => this.mapSubmission(response)),
      catchError(() => of(null))
    );
  }

  submitQuiz(quizId: string, answers: any[], score?: number | null): Observable<QuizAttempt> {
    return this.http.post<QuizSubmissionResponse>(
      `${this.baseUrl}/quiz/${quizId}/submit`,
      { answers, score: score ?? undefined },
      { headers: this.headers.buildHeaders() }
    ).pipe(map(response => this.mapSubmission(response)));
  }

  listSubmissions(quizId: string): Observable<QuizAttempt[]> {
    return this.http.get<QuizSubmissionResponse[]>(
      `${this.baseUrl}/quiz/${quizId}/submissions`,
      { headers: this.headers.buildHeaders() }
    ).pipe(map(responses => responses.map(response => this.mapSubmission(response))));
  }

  gradeSubmission(submissionId: string, score: number): Observable<QuizAttempt> {
    const request: QuizSubmissionGradeRequest = { score };
    return this.http.put<QuizSubmissionResponse>(
      `${this.baseUrl}/quiz/submission/${submissionId}/grade`,
      request,
      { headers: this.headers.buildHeaders() }
    ).pipe(map(response => this.mapSubmission(response)));
  }

  getLeaderboard(quizId: string): Observable<QuizAttempt[]> {
    return this.http.get<QuizSubmissionResponse[]>(
      `${this.baseUrl}/quiz/${quizId}/leaderboard`,
      { headers: this.headers.buildHeaders() }
    ).pipe(map(responses => responses.map(response => this.mapSubmission(response))));
  }

  getMyResults(): Observable<QuizAttempt[]> {
    return this.http.get<QuizSubmissionResponse[]>(
      `${this.baseUrl}/student/my-results`,
      { headers: this.headers.buildHeaders() }
    ).pipe(map(responses => responses.map(response => this.mapSubmission(response))));
  }

  private mapSubmission(response: QuizSubmissionResponse): QuizAttempt {
    return {
      id: response.id,
      courseId: response.courseId,
      studentId: response.studentId,
      lessonId: response.lessonId ?? undefined,
      quizId: response.quizId,
      score: response.score ?? null,
      passingScore: response.passingScore ?? null,
      passed: response.passed ?? null,
      attemptedAt: response.submittedAt,
      answers: response.answers ?? [],
      gradedAt: response.gradedAt ?? null,
      gradedBy: response.gradedBy ?? null
    };
  }
}
