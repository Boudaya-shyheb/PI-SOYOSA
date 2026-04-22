import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { QuizService, Quiz, QuizQuestion } from '../../services/quiz.service';
import { QuizAttemptService } from '../../services/quiz-attempt.service';
import { NotificationService } from '../../services/notification.service';
import { LoadingService } from '../../services/loading.service';

@Component({
  selector: 'app-quiz',
  templateUrl: './quiz.component.html',
  styleUrls: ['./quiz.component.css']
})
export class QuizComponent implements OnInit, OnDestroy {
  quiz: Quiz | null = null;
  loading = true;
  submitting = false;
  errorMessage = '';
  answers: number[] = [];
  submitted = false;
  scorePercent: number | null = null;
  passed: boolean | null = null;
  passingScore: number = 70;
  timeLeftSec = 0;
  private timerId: number | null = null;
  private courseId = '';
  private lessonId = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quizService: QuizService,
    private quizAttempts: QuizAttemptService,
    private notifications: NotificationService,
    private loadingService: LoadingService
  ) { }

  ngOnInit(): void {
    this.courseId = this.route.snapshot.paramMap.get('courseId') ?? '';
    this.lessonId = this.route.snapshot.paramMap.get('lessonId') ?? '';

    if (!this.courseId || !this.lessonId) {
      this.errorMessage = 'Quiz not found.';
      this.loading = false;
      return;
    }

    this.loadQuiz();
  }

  ngOnDestroy(): void {
    this.stopTimer();
  }

  private loadQuiz(): void {
    this.loadingService.start('quiz-load');

    this.quizService.getQuizByLesson(this.lessonId).subscribe({
      next: (quiz: Quiz | null) => {
        if (!quiz) {
          this.errorMessage = 'No quiz configured for this lesson.';
          this.loading = false;
          this.loadingService.stop('quiz-load');
          return;
        }

        this.quiz = quiz;
        this.passingScore = quiz.passingScore;
        this.answers = new Array(quiz.questions.length).fill(-1);
        this.timeLeftSec = Math.max(quiz.timeLimitMin, 1) * 60;

        this.checkExistingSubmission();
      },
      error: () => {
        this.errorMessage = 'Failed to load quiz.';
        this.loading = false;
        this.loadingService.stop('quiz-load');
      }
    });
  }

  private checkExistingSubmission(): void {
    this.quizAttempts.getMySubmission(this.lessonId).subscribe({
      next: submission => {
        if (submission) {
          this.submitted = true;
          this.scorePercent = submission.score ?? null;
          this.passed = submission.score !== null ? submission.score >= this.passingScore : null;
          this.answers = this.mergeAnswers(submission.answers ?? [], this.answers.length);
          this.errorMessage = '';
        } else {
          this.startTimer();
        }
        this.loading = false;
        this.loadingService.stop('quiz-load');
      },
      error: () => {
        this.startTimer();
        this.loading = false;
        this.loadingService.stop('quiz-load');
      }
    });
  }

  selectAnswer(questionIndex: number, optionIndex: number): void {
    if (this.submitted || this.submitting) {
      return;
    }
    this.answers[questionIndex] = optionIndex;
  }

  submitQuiz(): void {
    if (!this.quiz || this.submitted || this.submitting) {
      return;
    }
    if (this.answers.some(answer => answer < 0)) {
      this.notifications.warning('Please answer all questions before submitting.');
      return;
    }

    this.submitting = true;
    this.loadingService.start('quiz-submit');
    this.stopTimer();

    // Calculate score locally for immediate feedback
    const correct = this.quiz.questions.reduce((count: number, question: QuizQuestion, index: number) => {
      return count + (this.answers[index] === question.correctIndex ? 1 : 0);
    }, 0);
    const calculatedScore = Math.round((correct / this.quiz.questions.length) * 100);

    this.quizAttempts.submitQuiz(this.quiz.id, this.answers, calculatedScore).subscribe({
      next: submission => {
        this.submitted = true;
        this.submitting = false;
        this.loadingService.stop('quiz-submit');

        this.scorePercent = submission.score ?? calculatedScore;
        this.passed = this.scorePercent >= this.passingScore;

        if (this.passed) {
          this.notifications.quizPassed(this.scorePercent, this.passingScore);
        } else {
          this.notifications.quizFailed(this.scorePercent, this.passingScore);
        }
      },
      error: (error) => {
        const message = error?.error?.message || 'Unable to submit quiz.';
        this.errorMessage = message;
        this.notifications.error(message);
        this.submitting = false;
        this.loadingService.stop('quiz-submit');
        this.startTimer();
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/courses', this.courseId]);
  }

  isCorrect(question: QuizQuestion, optionIndex: number, questionIndex: number): boolean {
    if (!this.submitted) {
      return false;
    }
    return this.answers[questionIndex] === optionIndex && question.correctIndex === optionIndex;
  }

  isIncorrect(question: QuizQuestion, optionIndex: number, questionIndex: number): boolean {
    if (!this.submitted) {
      return false;
    }
    return this.answers[questionIndex] === optionIndex && question.correctIndex !== optionIndex;
  }

  isCorrectAnswer(question: QuizQuestion, optionIndex: number): boolean {
    return this.submitted && question.correctIndex === optionIndex;
  }

  private startTimer(): void {
    this.stopTimer();
    this.timerId = window.setInterval(() => {
      if (this.timeLeftSec <= 1) {
        this.timeLeftSec = 0;
        this.notifications.warning('Time is up! Submitting your quiz...');
        this.submitQuiz();
        return;
      }
      this.timeLeftSec -= 1;
    }, 1000);
  }

  private stopTimer(): void {
    if (this.timerId !== null) {
      clearInterval(this.timerId);
      this.timerId = null;
    }
  }

  private mergeAnswers(saved: number[], length: number): number[] {
    const base = new Array(length).fill(-1);
    saved.forEach((value, index) => {
      if (index < base.length) {
        base[index] = value;
      }
    });
    return base;
  }

  formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  }

  getTimerProgress(): number {
    if (!this.quiz) return 100;
    const totalSeconds = Math.max(this.quiz.timeLimitMin, 1) * 60;
    return (this.timeLeftSec / totalSeconds) * 100;
  }

  getAnsweredCount(): number {
    return this.answers.filter(a => a >= 0).length;
  }

  getProgress(): number {
    if (!this.quiz || this.quiz.questions.length === 0) return 0;
    return (this.getAnsweredCount() / this.quiz.questions.length) * 100;
  }
}
