import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { combineLatest, forkJoin, Observable, of } from 'rxjs';
import { catchError, distinctUntilChanged, map, switchMap } from 'rxjs/operators';
import { CourseApiService } from '../../services/course-api.service';
import { ChapterApiService } from '../../services/chapter-api.service';
import { LessonApiService } from '../../services/lesson-api.service';
import { AuthService } from '../../services/auth.service';
import { EnrollmentApiService } from '../../services/enrollment-api.service';
import { QuizApiService } from '../../services/quiz-api.service';
import { QuizAttemptService } from '../../services/quiz-attempt.service';
import {
  CourseResponse,
  ChapterResponse,
  LessonResponse,
  EnrollmentResponse,
  CourseReviewResponse,
  CourseReviewSummaryResponse
} from '../../models/api-models';
import { QuizAttempt } from '../../models/assessment-models';

export interface ChapterWithLessons extends ChapterResponse {
  lessons: LessonWithProgress[];
  expanded: boolean;
}

export interface LessonWithProgress extends LessonResponse {
  completed: boolean;
  duration: string;
}

interface MasterySkill {
  key: string;
  label: string;
  score: number;
  level: 'weak' | 'growing' | 'strong';
  tip: string;
}

interface AdaptiveRecommendation {
  title: string;
  reason: string;
  route: string[];
  cta: string;
}

@Component({
  selector: 'app-course-view',
  templateUrl: './course-view.component.html',
  styleUrls: ['./course-view.component.css']
})
export class CourseViewComponent implements OnInit {
  courseId = '';
  course: CourseResponse | null = null;
  enrollment: EnrollmentResponse | null = null;
  chapters: ChapterWithLessons[] = [];
  loading = true;
  errorMessage = '';

  allLessonsCompleted = false;
  finalQuizPassed = false;
  certificateEligible = false;
  showCertificate = false;
  userName = 'Student Name';
  courseAttempts: QuizAttempt[] = [];

  reviews: CourseReviewResponse[] = [];
  reviewSummary: CourseReviewSummaryResponse = {
    courseId: '',
    averageRating: 0,
    totalReviews: 0
  };
  reviewRating = 0;
  reviewComment = '';
  canSubmitReview = false;
  reviewSubmitting = false;
  reviewMessage = '';

  masterySkills: MasterySkill[] = [];
  adaptiveRecommendation: AdaptiveRecommendation = {
    title: 'Start your learning path',
    reason: 'Open your first lesson to begin building momentum and track your mastery.',
    route: ['/courses'],
    cta: 'Open Course'
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseApi: CourseApiService,
    private chapterApi: ChapterApiService,
    private lessonApi: LessonApiService,
    private auth: AuthService,
    private enrollmentApi: EnrollmentApiService,
    private quizApi: QuizApiService,
    private quizAttempts: QuizAttemptService
  ) {}

  ngOnInit(): void {
    const paramStreams = this.route.pathFromRoot.map(activeRoute => activeRoute.paramMap);

    combineLatest(paramStreams).pipe(
      map(paramMaps => {
        for (let i = paramMaps.length - 1; i >= 0; i--) {
          const id = paramMaps[i].get('courseId');
          if (id) {
            return id;
          }
        }
        return '';
      }),
      distinctUntilChanged()
    ).subscribe(courseId => {
      if (!courseId || courseId === this.courseId) {
        return;
      }

      this.courseId = courseId;
      this.handleEntryState();
    });
  }

  private handleEntryState(): void {
    const paymentSuccess = this.route.snapshot.queryParamMap.get('payment_success') === 'true';
    const sessionId = this.route.snapshot.queryParamMap.get('session_id');

    if (paymentSuccess && sessionId) {
      this.loading = true;
      this.enrollmentApi.confirmPayment(sessionId).pipe(
        catchError(() => of(''))
      ).subscribe(() => this.loadCourseData());
      return;
    }

    this.loadCourseData();
  }

  loadCourseData(): void {
    this.loading = true;
    this.courseId = this.route.snapshot.paramMap.get('courseId') ?? this.route.snapshot.paramMap.get('id') ?? '';
    this.errorMessage = '';

    if (!this.auth.isAuthenticated()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }

    this.courseApi.getCourse(this.courseId).pipe(
      switchMap(course => {
        this.course = course;

        if (this.auth.isStudent()) {
          return this.enrollmentApi.getEnrollment(this.courseId).pipe(
            catchError(() => of(null)),
            switchMap(enrollment => {
              this.enrollment = enrollment;

              if (!enrollment || enrollment.status !== 'ACTIVE') {
                if (course.isPaid) {
                  this.redirectToPayment(course);
                  return of({ chaptersWithLessons: [] as ChapterWithLessons[], attempts: [] as QuizAttempt[] });
                } else {
                  return this.enrollmentApi.requestEnrollment(this.courseId).pipe(
                    switchMap((newEnrollment: EnrollmentResponse) => {
                      this.enrollment = newEnrollment;
                      return this.loadChaptersAndAttempts();
                    }),
                    catchError(() => {
                      this.errorMessage = 'Failed to enroll in course.';
                      return of({ chaptersWithLessons: [] as ChapterWithLessons[], attempts: [] as QuizAttempt[] });
                    })
                  );
                }
              }

              return this.loadChaptersAndAttempts();
            })
          );
        }

        return this.loadChaptersAndAttempts();
      }),
      catchError(() => {
        this.errorMessage = 'Failed to load course data.';
        return of({ chaptersWithLessons: [] as ChapterWithLessons[], attempts: [] as QuizAttempt[] });
      })
    ).subscribe(result => {
      this.chapters = result.chaptersWithLessons;
      this.courseAttempts = result.attempts;
      this.checkAllLessonsCompleted();
      
      if (this.auth.isStudent()) {
        this.refreshCertificateEligibility();
        this.refreshAdaptiveInsights();
        this.updateReviewEligibility();
      }
      
      this.loadReviews();
      this.loadReviewSummary();
      this.loading = false;
    });
  }

  setRating(rating: number): void {
    this.reviewRating = rating;
  }

  submitReview(): void {
    this.reviewMessage = '';

    if (!this.canSubmitReview) {
      this.reviewMessage = 'Only enrolled students can submit a review.';
      return;
    }

    const comment = this.reviewComment.trim();
    if (this.reviewRating < 1 || this.reviewRating > 5) {
      this.reviewMessage = 'Please select a star rating from 1 to 5.';
      return;
    }
    if (!comment) {
      this.reviewMessage = 'Please write a short comment.';
      return;
    }

    this.reviewSubmitting = true;
    this.courseApi.upsertCourseReview(this.courseId, {
      rating: this.reviewRating,
      comment
    }).subscribe({
      next: () => {
        this.reviewSubmitting = false;
        this.reviewMessage = 'Thanks for your feedback!';
        this.loadReviews();
        this.loadReviewSummary();
      },
      error: (error) => {
        this.reviewSubmitting = false;
        this.reviewMessage = error?.error?.message || 'Unable to submit your review right now.';
      }
    });
  }

  asStars(value: number): string {
    const rounded = Math.max(0, Math.min(5, Math.round(value)));
    return '★'.repeat(rounded) + '☆'.repeat(5 - rounded);
  }

  shortUser(userId: string): string {
    if (!userId) {
      return 'Student';
    }
    if (userId.length <= 6) {
      return userId;
    }
    return `${userId.slice(0, 3)}...${userId.slice(-3)}`;
  }

  private loadReviews(): void {
    this.courseApi.getCourseReviews(this.courseId).pipe(
      catchError(() => of([] as CourseReviewResponse[]))
    ).subscribe(reviews => {
      this.reviews = reviews;
      const mine = reviews.find(item => item.mine);
      if (mine) {
        this.reviewRating = mine.rating;
        this.reviewComment = mine.comment;
      }
    });
  }

  private loadReviewSummary(): void {
    this.courseApi.getCourseReviewSummary(this.courseId).pipe(
      catchError(() => of({
        courseId: this.courseId,
        averageRating: 0,
        totalReviews: 0
      } as CourseReviewSummaryResponse))
    ).subscribe(summary => {
      this.reviewSummary = summary;
    });
  }

  private updateReviewEligibility(): void {
    if (!this.auth.isStudent()) {
      this.canSubmitReview = false;
      return;
    }

    const status = this.enrollment?.status;
    this.canSubmitReview = status === 'ACTIVE' || status === 'COMPLETED';
  }

  private loadChaptersAndAttempts(): Observable<{ chaptersWithLessons: ChapterWithLessons[]; attempts: QuizAttempt[] }> {
    return this.chapterApi.listChapters(this.courseId).pipe(
      catchError(() => of([] as ChapterResponse[])),
      switchMap(chapters => {
        if (chapters.length === 0) {
          return of({ chaptersWithLessons: [] as ChapterWithLessons[], attempts: [] as QuizAttempt[] });
        }

        const lessonRequests = chapters.map(chapter =>
          this.lessonApi.listLessonsByChapter(chapter.id).pipe(
            catchError(() => of([] as LessonResponse[]))
          )
        );

        return forkJoin(lessonRequests).pipe(
          catchError(() => of([] as LessonResponse[][])),
          switchMap(lessonArrays => {
            const chaptersWithLessons: ChapterWithLessons[] = chapters.map((chapter, index) => ({
              ...chapter,
              lessons: (lessonArrays[index] || []).map((lesson: LessonResponse) => ({
                ...lesson,
                completed: this.getLessonProgress(lesson.id),
                duration: this.estimateDuration(lesson)
              })),
              expanded: index === 0
            }));

            if (!this.auth.isStudent()) {
              return of({ chaptersWithLessons, attempts: [] as QuizAttempt[] });
            }

            return this.quizAttempts.getAttempts(this.courseId).pipe(
              catchError(() => of([] as QuizAttempt[])),
              map(attempts => ({ chaptersWithLessons, attempts }))
            );
          })
        );
      })
    );
  }

  private redirectToPayment(course: CourseResponse): void {
    this.router.navigate(['/payment'], {
      queryParams: {
        courseId: this.courseId,
        title: course.title,
        amount: course.price ?? 0,
        returnUrl: `/course-view/${this.courseId}`
      }
    });
  }

  private getLessonProgress(lessonId: string): boolean {
    const progressKey = `lesson_progress_${this.courseId}`;
    const progress = localStorage.getItem(progressKey);
    if (progress) {
      try {
        const completedLessons: string[] = JSON.parse(progress);
        return completedLessons.includes(lessonId);
      } catch {
        return false;
      }
    }
    return false;
  }

  private estimateDuration(lesson: LessonResponse): string {
    const durations = ['10mins', '15mins', '20mins', '25mins', '30mins', '35mins'];
    const index = (lesson.orderIndex || 0) % durations.length;
    return durations[index];
  }

  onLessonToggle(lessonId: string, completed: boolean): void {
    const progressKey = `lesson_progress_${this.courseId}`;
    let completedLessons: string[] = [];
    const progress = localStorage.getItem(progressKey);
    if (progress) {
      try {
        completedLessons = JSON.parse(progress);
      } catch {
        completedLessons = [];
      }
    }

    if (completed && !completedLessons.includes(lessonId)) {
      completedLessons.push(lessonId);
    } else if (!completed) {
      completedLessons = completedLessons.filter(id => id !== lessonId);
    }

    localStorage.setItem(progressKey, JSON.stringify(completedLessons));

    for (const chapter of this.chapters) {
      const lesson = chapter.lessons.find(l => l.id === lessonId);
      if (lesson) {
        lesson.completed = completed;
        break;
      }
    }

    this.checkAllLessonsCompleted();
    this.refreshCertificateEligibility();
    this.refreshAdaptiveInsights();
  }

  private checkAllLessonsCompleted(): void {
    if (this.chapters.length === 0) {
      this.allLessonsCompleted = false;
      return;
    }

    const allLessons = this.chapters.flatMap(c => c.lessons);
    if (allLessons.length === 0) {
      this.allLessonsCompleted = false;
      return;
    }

    this.allLessonsCompleted = allLessons.every(lesson => lesson.completed);
  }

  onChapterToggle(chapterId: string): void {
    const chapter = this.chapters.find(c => c.id === chapterId);
    if (chapter) {
      chapter.expanded = !chapter.expanded;
    }
  }

  openCertificate(): void {
    if (!this.certificateEligible) {
      return;
    }
    this.showCertificate = true;
  }

  closeCertificate(): void {
    this.showCertificate = false;
  }

  get completionPercent(): number {
    const allLessons = this.chapters.flatMap(chapter => chapter.lessons);
    if (allLessons.length === 0) {
      return 0;
    }

    const completedLessons = allLessons.filter(lesson => lesson.completed).length;
    return Math.round((completedLessons / allLessons.length) * 100);
  }

  get sectionCode(): string {
    const level = this.course?.level ?? 'GEN';
    const suffix = this.courseId ? this.courseId.slice(0, 6).toUpperCase() : '000000';
    return `${level}-${suffix}`;
  }

  private refreshAdaptiveInsights(): void {
    const completionScore = this.completionPercent;
    const scoredAttempts = this.courseAttempts.filter(attempt => attempt.score !== null && attempt.score !== undefined);
    const averageQuiz = scoredAttempts.length > 0
      ? Math.round(scoredAttempts.reduce((sum, attempt) => sum + (attempt.score ?? 0), 0) / scoredAttempts.length)
      : 0;

    const passRate = scoredAttempts.length > 0
      ? Math.round((scoredAttempts.filter(attempt => attempt.passed || ((attempt.score ?? 0) >= 70)).length / scoredAttempts.length) * 100)
      : 0;

    const practiceConsistency = Math.min(
      100,
      Math.round((completionScore * 0.7) + (Math.min(scoredAttempts.length, 5) * 6))
    );

    const examReadiness = Math.round((completionScore * 0.6) + (averageQuiz * 0.4));

    this.masterySkills = [
      this.toMastery('completion', 'Lesson Completion', completionScore, 'Finish one lesson at a time and keep order to reduce knowledge gaps.'),
      this.toMastery('quiz', 'Quiz Accuracy', averageQuiz, 'Review missed questions and retry the same topic after a short break.'),
      this.toMastery('consistency', 'Practice Consistency', practiceConsistency, 'Study in short daily sessions instead of long irregular sessions.'),
      this.toMastery('readiness', 'Exam Readiness', examReadiness, 'Aim for 70%+ completion and 70%+ quiz average before the final assessment.')
    ];

    this.adaptiveRecommendation = this.buildAdaptiveRecommendation(averageQuiz, passRate);
  }

  private toMastery(key: string, label: string, score: number, tip: string): MasterySkill {
    const normalized = Math.max(0, Math.min(100, Math.round(score)));
    let level: 'weak' | 'growing' | 'strong' = 'weak';
    if (normalized >= 75) {
      level = 'strong';
    } else if (normalized >= 45) {
      level = 'growing';
    }

    return {
      key,
      label,
      score: normalized,
      level,
      tip
    };
  }

  private buildAdaptiveRecommendation(averageQuiz: number, passRate: number): AdaptiveRecommendation {
    const lessons = this.chapters.flatMap(chapter => chapter.lessons);
    const firstIncomplete = lessons.find(lesson => !lesson.completed);

    if (firstIncomplete && averageQuiz > 0 && averageQuiz < 60) {
      return {
        title: 'Adaptive Review Suggested',
        reason: `Your quiz average is ${averageQuiz}%. Review this lesson before moving forward to improve retention.`,
        route: ['/lessons', firstIncomplete.id],
        cta: 'Review Lesson'
      };
    }

    if (firstIncomplete) {
      return {
        title: 'Next Best Lesson',
        reason: 'Continue with the first incomplete lesson to keep your learning flow strong.',
        route: ['/lessons', firstIncomplete.id],
        cta: 'Continue Learning'
      };
    }

    const finalExamId = this.getFinalAssessmentLessonId();
    if (this.allLessonsCompleted && !this.finalQuizPassed && finalExamId) {
      return {
        title: 'Final Assessment Ready',
        reason: passRate > 0
          ? `Your current quiz pass rate is ${passRate}%. Take the final assessment to unlock your certificate.`
          : 'All lessons are completed. Take the final assessment to unlock your certificate.',
        route: ['/courses', this.courseId, 'lessons', finalExamId, 'quiz'],
        cta: 'Take Final Quiz'
      };
    }

    if (this.certificateEligible) {
      return {
        title: 'Certificate Unlocked',
        reason: 'Excellent work. You completed all lessons and passed the final requirement.',
        route: ['/courses', this.courseId],
        cta: 'View Certificate'
      };
    }

    return {
      title: 'Personal Tutor Tip',
      reason: 'Revisit your lowest mastery skill in the map below, then continue with the next lesson.',
      route: ['/courses', this.courseId],
      cta: 'Open Course'
    };
  }

  private refreshCertificateEligibility(): void {
    if (!this.auth.isStudent() || !this.allLessonsCompleted) {
      this.finalQuizPassed = false;
      this.certificateEligible = false;
      return;
    }

    const finalLessonId = this.getFinalAssessmentLessonId();
    if (!finalLessonId) {
      this.finalQuizPassed = true;
      this.certificateEligible = true;
      return;
    }

    this.quizApi.getQuizByLesson(finalLessonId).subscribe({
      next: quiz => {
        if (!quiz) {
          this.finalQuizPassed = true;
          this.certificateEligible = true;
          return;
        }

        this.quizAttempts.getMySubmission(finalLessonId).subscribe({
          next: submission => {
            const passed = submission?.passed ?? (submission?.score !== null && submission?.score !== undefined
              ? submission.score >= quiz.passingScore
              : false);
            this.finalQuizPassed = !!passed;
            this.certificateEligible = this.allLessonsCompleted && this.finalQuizPassed;
          },
          error: () => {
            this.finalQuizPassed = false;
            this.certificateEligible = false;
          }
        });
      },
      error: () => {
        this.finalQuizPassed = false;
        this.certificateEligible = false;
      }
    });
  }

  private getFinalAssessmentLessonId(): string | null {
    const lessons = this.chapters.flatMap(chapter => chapter.lessons);
    const explicit = lessons.find(lesson => {
      const normalized = lesson.title.toLowerCase();
      return normalized.includes('final') || normalized.includes('exam') || normalized.includes('quiz');
    });
    return explicit?.id ?? null;
  }
}
