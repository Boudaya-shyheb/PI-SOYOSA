import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { QuizApiService } from './quiz-api.service';
import { Quiz, QuizQuestion } from '../models/quiz-models';
export { Quiz, QuizQuestion } from '../models/quiz-models';

@Injectable({ providedIn: 'root' })
export class QuizService {
  private readonly cache = new Map<string, Quiz>();

  constructor(private quizApi: QuizApiService) {}

  getQuiz(lessonId: string): Quiz | null {
    return this.cache.get(lessonId) ?? null;
  }

  getQuizByLesson(lessonId: string): Observable<Quiz | null> {
    return this.quizApi.getQuizByLesson(lessonId).pipe(
      map(quiz => {
        const mapped = quiz as Quiz | null;
        if (mapped) {
          this.cache.set(lessonId, mapped);
        }
        return mapped;
      })
    );
  }

  getQuizByCourse(courseId: string): Quiz[] {
    return Array.from(this.cache.values()).filter(quiz => quiz.courseId === courseId);
  }

  saveQuiz(quiz: Quiz): Observable<Quiz> {
    const payload = {
      courseId: quiz.courseId,
      lessonId: quiz.lessonId,
      title: quiz.title,
      timeLimitMin: quiz.timeLimitMin,
      passingScore: quiz.passingScore,
      maxAttempts: quiz.maxAttempts ?? 1,
      cooldownMin: quiz.cooldownMin ?? 0,
      questions: quiz.questions
    };

    const hasPersistedId = !!quiz.id && !quiz.id.startsWith('quiz-');
    if (hasPersistedId) {
      return this.quizApi.updateQuiz(quiz.id, {
        title: payload.title,
        timeLimitMin: payload.timeLimitMin,
        passingScore: payload.passingScore,
        maxAttempts: payload.maxAttempts,
        cooldownMin: payload.cooldownMin,
        questions: payload.questions
      }).pipe(
        map(updated => {
          const saved = updated as Quiz;
          this.cache.set(quiz.lessonId, saved);
          return saved;
        })
      );
    }

    return this.quizApi.createQuiz(payload).pipe(
      map(created => {
        const saved = created as Quiz;
        this.cache.set(quiz.lessonId, saved);
        return saved;
      })
    );
  }

  deleteQuiz(lessonId: string): Observable<void> {
    return this.quizApi.getQuizByLesson(lessonId).pipe(
      switchMap(quiz => {
        if (!quiz?.id) {
          this.cache.delete(lessonId);
          return of(undefined);
        }
        return this.quizApi.deleteQuiz(quiz.id).pipe(
          map(() => {
            this.cache.delete(lessonId);
            return undefined;
          })
        );
      })
    );
  }

  createQuiz(
    courseId: string,
    lessonId: string,
    title: string,
    timeLimitMin: number,
    passingScore: number,
    questions: QuizQuestion[],
    maxAttempts = 1,
    cooldownMin = 0
  ): Quiz {
    const now = new Date().toISOString();
    return {
      id: this.generateId(),
      courseId,
      lessonId,
      title,
      timeLimitMin,
      passingScore,
      maxAttempts,
      cooldownMin,
      questions,
      createdAt: now,
      updatedAt: now
    };
  }

  private generateId(): string {
    return `quiz-${Math.random().toString(36).slice(2, 10)}`;
  }
}
