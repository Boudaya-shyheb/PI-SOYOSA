import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { LessonApiService } from '../../services/lesson-api.service';
import { MaterialApiService } from '../../services/material-api.service';
import { QuizService } from '../../services/quiz.service';
import { QuizAttemptService } from '../../services/quiz-attempt.service';
import { AuthService } from '../../services/auth.service';
import { ProgressTrackingService } from '../../services/progress-tracking.service';
import { LessonResponse, MaterialResponse } from '../../models/api-models';
import { QuizAttempt } from '../../models/assessment-models';
import { Quiz } from '../../models/quiz-models';
import { COURSE_API_BASE_URL } from '../../services/api.config';

interface ContentPage {
  type: 'material' | 'quiz';
  title: string;
  material?: MaterialResponse;
  quiz?: Quiz;
}

@Component({
  selector: 'app-lesson-details',
  templateUrl: './lesson-details.component.html',
  styleUrls: ['./lesson-details.component.css']
})
export class LessonDetailsComponent implements OnInit {
  lessonId = '';
  lesson: LessonResponse | null = null;
  materials: MaterialResponse[] = [];
  quiz: Quiz | null = null;
  quizAttempts: QuizAttempt[] = [];
  
  currentPageIndex = 0;
  contentPages: ContentPage[] = [];
  
  loading = true;
  errorMessage = '';
  isStudent: boolean;
  isTeacher: boolean;
  isAdmin: boolean;
  
  takingQuiz = false;
  quizAnswers: Record<number, number> = {};
  quizScore: number | null = null;
  showQuizResults = false;
  lessonCompleted = false;
  private autoplayRequested = false;
  submissions: QuizAttempt[] = [];
  gradingScores: Record<string, number> = {};
  
  // Teacher content management
  showContentEditor = false;
  editingMaterial: any = this.getEmptyMaterial();
  editingMaterialId: string | null = null;
  selectedFile: File | null = null;
  uploadProgress = 0;
  savingMaterial = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private location: Location,
    private lessonApi: LessonApiService,
    private materialsApi: MaterialApiService,
    private quizApi: QuizService,
    private quizAttemptApi: QuizAttemptService,
    private auth: AuthService,
    private progressTracking: ProgressTrackingService,
    private sanitizer: DomSanitizer
  ) {
    this.isStudent = this.auth.isStudent();
    this.isTeacher = this.auth.isTeacher();
    this.isAdmin = this.auth.isAdmin();
  }

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      this.autoplayRequested = params.get('autoplay') === '1';
    });
    this.route.paramMap.subscribe(params => {
      const lessonId = params.get('lessonId') || '';
      if (lessonId && lessonId !== this.lessonId) {
        this.lessonId = lessonId;
        this.loadLesson();
      }
    });
  }

  loadLesson(): void {
    this.loading = true;
    this.errorMessage = '';

    this.lessonApi.getLesson(this.lessonId).pipe(
      switchMap(lesson => {
        this.lesson = lesson;
        return forkJoin({
          materials: this.materialsApi.listMaterials(this.lessonId).pipe(catchError(() => of([]))),
          quiz: this.quizApi.getQuizByLesson(this.lessonId).pipe(catchError(() => of(null))),
          attempts: this.isStudent 
            ? this.quizAttemptApi.getAttemptsByLesson(this.lessonId).pipe(catchError(() => of([])))
            : of([])
        });
      }),
      catchError(error => {
        this.errorMessage = 'Failed to load lesson. Please try again.';
        this.loading = false;
        return of(null);
      })
    ).subscribe(result => {
      if (result) {
        this.materials = result.materials || [];
        this.quiz = result.quiz;
        this.quizAttempts = result.attempts || [];
        if (this.quizAttempts.length > 0) {
          this.showQuizResults = true;
          this.quizScore = this.quizAttempts[0].score ?? null;
          this.takingQuiz = false;
        }
        this.buildContentPages();
        this.triggerAutoplayIfNeeded();

        if (this.isTeacher || this.isAdmin) {
          this.loadSubmissions(this.quiz?.id ?? this.lessonId);
        }
        
        // Track lesson access for progress
        if (this.lesson && this.isStudent) {
          this.progressTracking.updateCurrentLesson(
            this.lesson.courseId, 
            this.lesson.id, 
            this.lesson.chapterId
          );
          
          // Check if lesson is already completed
          this.lessonCompleted = this.progressTracking.isLessonCompleted(
            this.lesson.courseId, 
            this.lesson.id
          );
        }
      }
      this.loading = false;
    });
  }

  private buildContentPages(): void {
    this.contentPages = [];

    // Add materials as pages
    this.materials.forEach((material, index) => {
      this.contentPages.push({
        type: 'material',
        title: `${material.title} (Page ${this.contentPages.length + 1})`,
        material
      });
    });

    // Add quiz as last page (if exists and student)
    if (this.quiz && this.isStudent) {
      this.contentPages.push({
        type: 'quiz',
        title: `${this.quiz.title} (Final Quiz)`,
        quiz: this.quiz
      });
    }

    this.currentPageIndex = 0;
  }

  get currentPage(): ContentPage | null {
    return this.contentPages[this.currentPageIndex] || null;
  }

  get isFirstPage(): boolean {
    return this.currentPageIndex === 0;
  }

  get isLastPage(): boolean {
    return this.currentPageIndex === this.contentPages.length - 1;
  }

  get totalPages(): number {
    return this.contentPages.length;
  }

  goToPage(index: number): void {
    if (index >= 0 && index < this.contentPages.length) {
      this.currentPageIndex = index;
    }
  }

  nextPage(): void {
    if (!this.isLastPage) {
      this.currentPageIndex++;
    }
  }

  previousPage(): void {
    if (!this.isFirstPage) {
      this.currentPageIndex--;
    }
  }

  startQuiz(): void {
    if (!this.quiz) {
      return;
    }
    if (this.quizAttempts.length > 0) {
      this.errorMessage = 'Quiz already completed.';
      this.showQuizResults = true;
      this.quizScore = this.quizAttempts[0].score ?? null;
      return;
    }
    this.takingQuiz = true;
    this.quizAnswers = {};
    this.quizScore = null;
    this.showQuizResults = false;
  }

  selectAnswer(questionIndex: number, optionIndex: number): void {
    this.quizAnswers[questionIndex] = optionIndex;
  }

  submitQuiz(): void {
    if (!this.quiz || this.takingQuiz === false) {
      return;
    }

    const answers = this.quiz.questions.map((_, index) => this.quizAnswers[index] ?? -1);
    if (answers.some(answer => answer < 0)) {
      this.errorMessage = 'Please answer all questions before submitting.';
      return;
    }
    let calculatedScore: number | null = null;
    if (this.quiz.questions.length > 0) {
      const correct = this.quiz.questions.reduce((count, question, index) => {
        return count + (answers[index] === question.correctIndex ? 1 : 0);
      }, 0);
      calculatedScore = Math.round((correct / this.quiz.questions.length) * 100);
    }
    this.quizAttemptApi.submitQuiz(this.quiz.id, answers, calculatedScore).subscribe({
      next: submission => {
        this.quizAttempts = [submission];
        this.quizScore = submission.score ?? null;
        this.showQuizResults = true;
        this.takingQuiz = false;
        window.alert('Quiz submitted successfully.');
        if (this.lesson?.courseId) {
          this.router.navigate(['/courses', this.lesson.courseId]);
        }
      },
      error: (error) => {
        const message = error?.error?.message || 'Unable to submit quiz.';
        this.errorMessage = message;
      }
    });
  }

  retakeQuiz(): void {
    if (this.quizAttempts.length > 0) {
      this.errorMessage = 'Quiz already completed.';
      return;
    }
    this.takingQuiz = false;
    this.quizAnswers = {};
    this.quizScore = null;
    this.showQuizResults = false;
  }

  markLessonComplete(): void {
    if (!this.lesson || !this.isStudent) return;
    
    // Mark as complete (we'll need to pass total lessons from course API later)
    // For now, we'll use a default value
    this.progressTracking.markLessonComplete(
      this.lesson.courseId,
      this.lesson.id,
      10 // TODO: Get actual total lessons from course data
    );
    
    this.lessonCompleted = true;
  }

  goBack(): void {
    this.location.back();
  }

  getAttemptStats() {
    if (this.quizAttempts.length === 0) return null;

    const scores = this.quizAttempts.map(a => a.score).filter((score): score is number => score !== null);
    if (scores.length === 0) {
      return {
        attempts: this.quizAttempts.length,
        bestScore: 0,
        lastScore: 0,
        averageScore: 0,
        passed: false
      };
    }
    const bestScore = Math.max(...scores);
    const lastScore = scores[scores.length - 1];
    const averageScore = Math.round(scores.reduce((a, b) => a + b, 0) / scores.length);

    return {
      attempts: this.quizAttempts.length,
      bestScore,
      lastScore,
      averageScore,
      passed: bestScore >= (this.quiz?.passingScore || 70)
    };
  }

  private loadSubmissions(quizId: string): void {
    this.quizAttemptApi.listSubmissions(quizId).subscribe({
      next: submissions => {
        this.submissions = submissions;
        submissions.forEach(submission => {
          if (submission.score !== null && submission.score !== undefined) {
            this.gradingScores[submission.id] = submission.score;
          }
        });
      },
      error: () => {
        this.errorMessage = 'Unable to load quiz submissions.';
      }
    });
  }

  saveGrade(submission: QuizAttempt): void {
    const score = this.gradingScores[submission.id];
    if (score === null || score === undefined || Number.isNaN(score)) {
      this.errorMessage = 'Score is required.';
      return;
    }
    this.quizAttemptApi.gradeSubmission(submission.id, score).subscribe({
      next: updated => {
        const index = this.submissions.findIndex(item => item.id === updated.id);
        if (index >= 0) {
          this.submissions[index] = updated;
        }
      },
      error: (error) => {
        const message = error?.error?.message || 'Unable to save score.';
        this.errorMessage = message;
      }
    });
  }

  materialFileUrl(materialId: string): string {
    return `${COURSE_API_BASE_URL}/materials/${materialId}/file`;
  }

  safeResourceUrl(value: string | null | undefined): SafeResourceUrl | null {
    if (!value) {
      return null;
    }
    return this.sanitizer.bypassSecurityTrustResourceUrl(value);
  }

  isPdfMaterial(material: MaterialResponse | undefined | null): boolean {
    if (!material?.hasFile || material.type === 'SLIDES') {
      return false;
    }
    return (material.fileType || '').toLowerCase().includes('pdf');
  }

  isImageMaterial(material: MaterialResponse | undefined | null): boolean {
    if (!material?.hasFile || material.type === 'SLIDES') {
      return false;
    }
    return (material.fileType || '').toLowerCase().startsWith('image/');
  }

  isOtherFileMaterial(material: MaterialResponse | undefined | null): boolean {
    if (!material?.hasFile) {
      return false;
    }
    if (material.type === 'SLIDES') {
      return false;
    }
    const fileType = (material.fileType || '').toLowerCase();
    if (fileType.startsWith('video/') || fileType.startsWith('audio/')) {
      return false;
    }
    if (this.isPdfMaterial(material) || this.isImageMaterial(material)) {
      return false;
    }
    return true;
  }

  private triggerAutoplayIfNeeded(): void {
    if (!this.autoplayRequested) {
      return;
    }
    this.autoplayRequested = false;
    const videoIndex = this.contentPages.findIndex(page => page.type === 'material' && page.material?.type === 'VIDEO');
    if (videoIndex >= 0) {
      this.currentPageIndex = videoIndex;
    }
    setTimeout(() => {
      const video = document.querySelector('video');
      if (video) {
        video.setAttribute('autoplay', 'true');
        const playPromise = (video as HTMLVideoElement).play();
        if (playPromise && typeof playPromise.catch === 'function') {
          playPromise.catch(() => {});
        }
      }
    }, 0);
  }

  // Teacher Content Management Methods
  toggleContentEditor(): void {
    this.showContentEditor = !this.showContentEditor;
    if (!this.showContentEditor) {
      this.cancelEdit();
    }
  }

  getEmptyMaterial(): any {
    return {
      title: '',
      type: 'TEXT',
      content: '',
      url: ''
    };
  }

  onFileSelected(event: any): void {
    const file = event.target.files?.[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  getAcceptedFileTypes(type: string): string {
    const types: Record<string, string> = {
      'VIDEO': 'video/mp4,video/webm,video/ogg',
      'AUDIO': 'audio/mpeg,audio/wav,audio/ogg',
      'PDF': 'application/pdf',
      'SLIDES': 'application/pdf,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation'
    };
    return types[type] || '*';
  }

  getFileTypeHint(type: string): string {
    const hints: Record<string, string> = {
      'VIDEO': 'Accepted: MP4, WebM, OGG (max 100MB)',
      'AUDIO': 'Accepted: MP3, WAV, OGG (max 50MB)',
      'PDF': 'Accepted: PDF files only (max 20MB)',
      'SLIDES': 'Accepted: PDF, PPT, PPTX (max 50MB)'
    };
    return hints[type] || '';
  }

  saveMaterial(): void {
    if (!this.editingMaterial.title) {
      this.errorMessage = 'Please enter a title';
      return;
    }

    this.savingMaterial = true;
    this.errorMessage = '';

    const request = {
      lessonId: this.lessonId,
      type: this.editingMaterial.type,
      title: this.editingMaterial.title,
      content: this.editingMaterial.content || null,
      url: this.editingMaterial.url || null
    };

    const saveOperation = this.editingMaterialId
      ? this.materialsApi.updateMaterial(this.editingMaterialId, request)
      : this.materialsApi.createMaterial(request);

    saveOperation.subscribe({
      next: (material) => {
        // If there's a file to upload, upload it
        if (this.selectedFile) {
          this.uploadMaterialFile(material.id);
        } else {
          this.loadLesson();
          this.cancelEdit();
          this.savingMaterial = false;
        }
      },
      error: (error) => {
        this.errorMessage = 'Failed to save material: ' + (error?.error?.message || 'Unknown error');
        this.savingMaterial = false;
      }
    });
  }

  uploadMaterialFile(materialId: string): void {
    if (!this.selectedFile) return;

    this.uploadProgress = 0;
    this.materialsApi.uploadMaterialFile(materialId, this.selectedFile).subscribe({
      next: (event) => {
        if ('percent' in event) {
          this.uploadProgress = event.percent;
        } else {
          // Upload complete
          this.uploadProgress = 100;
          setTimeout(() => {
            this.loadLesson();
            this.cancelEdit();
            this.savingMaterial = false;
            this.uploadProgress = 0;
          }, 500);
        }
      },
      error: (error) => {
        this.errorMessage = 'Failed to upload file: ' + (error?.error?.message || 'Unknown error');
        this.savingMaterial = false;
        this.uploadProgress = 0;
      }
    });
  }

  editMaterial(material: MaterialResponse): void {
    this.editingMaterialId = material.id;
    this.editingMaterial = {
      title: material.title,
      type: material.type,
      content: material.content || '',
      url: material.url || ''
    };
    this.showContentEditor = true;
    this.selectedFile = null;
  }

  cancelEdit(): void {
    this.editingMaterial = this.getEmptyMaterial();
    this.editingMaterialId = null;
    this.selectedFile = null;
    this.uploadProgress = 0;
  }

  deleteMaterialConfirm(materialId: string): void {
    if (confirm('Are you sure you want to delete this material?')) {
      this.materialsApi.deleteMaterial(materialId).subscribe({
        next: () => {
          this.loadLesson();
        },
        error: (error) => {
          this.errorMessage = 'Failed to delete material: ' + (error?.error?.message || 'Unknown error');
        }
      });
    }
  }

  onVideoError(): void {
    console.error('Video failed to load');
    this.errorMessage = 'Video failed to load. Please check the file or try again.';
  }
}
