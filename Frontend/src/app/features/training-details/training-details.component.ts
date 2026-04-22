import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { combineLatest, forkJoin, of } from 'rxjs';
import { catchError, distinctUntilChanged, map, switchMap } from 'rxjs/operators';
import { ChapterApiService } from '../../services/chapter-api.service';
import { CourseApiService } from '../../services/course-api.service';
import { LessonApiService } from '../../services/lesson-api.service';
import { MaterialApiService, FileUploadProgress } from '../../services/material-api.service';
import { EnrollmentApiService } from '../../services/enrollment-api.service';
import { CertificateService } from '../../services/certificate.service';
import { QuizAttemptService } from '../../services/quiz-attempt.service';
import { QuizService, Quiz, QuizQuestion } from '../../services/quiz.service';
import { QuizValidationService } from '../../services/quiz-validation.service';
import { AuthService } from '../../services/auth.service';
import { COURSE_API_BASE_URL } from '../../services/api.config';
import { ChapterCreateRequest, ChapterResponse, EnrollmentResponse, LessonCreateRequest, LessonResponse, MaterialCreateRequest, MaterialResponse, MaterialType, ProgressResponse } from '../../models/api-models';
import { QuizAttempt } from '../../models/assessment-models';

type Tab = 'About' | 'Content' | 'Annexes' | 'Forum';

interface SectionView {
  id: string;
  title: string;
  duration: string;
  completed: boolean;
  quizLessonId?: string;
  lessonId?: string;
  type?: 'LESSON' | 'QUIZ';
}

interface ChapterView {
  id: string;
  title: string;
  sections: SectionView[];
}

@Component({
  selector: 'app-training-details',
  templateUrl: './training-details.component.html',
  styleUrls: ['./training-details.component.css']
})
export class TrainingDetailsComponent {
  activeTab: Tab = 'Content';
  expandedChapters: string[] = [];
  chapters: ChapterView[] = [];
  courseTitle = 'English for adults';
  loading = true;
  errorMessage = '';
  successMessage = '';
  progress: ProgressResponse | null = null;
  enrollment: EnrollmentResponse | null = null;
  certificateAvailable = false;
  chaptersData: ChapterResponse[] = [];
  lessonsData: LessonResponse[] = [];
  materialsByLesson: Record<string, MaterialResponse[]> = {};
  materialTypes: MaterialType[] = ['VIDEO', 'AUDIO', 'TEXT', 'QUIZ', 'SLIDES', 'LINK'];
  quizByLesson: Record<string, Quiz> = {};
  courseId = '';
  private readonly passingScore = 70;
  enrolling = false;
  dragOver = false;
  uploadProgress: Record<string, number> = {};
  uploadStatus: Record<string, string> = {};
  uploading: Record<string, boolean> = {};
  courseHasContent = false;
  courseImageUrl = '';
  coursePrice = 0;
  isPaidCourse = false;

  creatingChapter = false;
  editingChapterId: string | null = null;
  newChapter: ChapterCreateRequest = { courseId: '', title: '', orderIndex: 1 };
  editChapter = { title: '', orderIndex: 1 };

  newLessons: Record<string, LessonCreateRequest> = {};
  editingLessonId: string | null = null;
  editLesson = { title: '', orderIndex: 1, xpReward: 1 };

  newMaterials: Record<string, MaterialCreateRequest> = {};
  editingMaterialId: string | null = null;
  editMaterial = { type: 'TEXT' as MaterialType, title: '', url: '', content: '' };

  newQuiz: Record<string, Quiz> = {};
  editingQuizLessonId: string | null = null;

  onFileSelected(event: Event, mode: 'create' | 'edit', lessonId: string): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      this.handleFileUpload(file, mode, lessonId);
    }
  }

  onFileDrop(event: DragEvent, mode: 'create' | 'edit', lessonId: string): void {
    event.preventDefault();
    this.dragOver = false;
    const file = event.dataTransfer?.files?.[0];
    if (file) {
      this.handleFileUpload(file, mode, lessonId);
    }
  }

  private handleFileUpload(file: File, mode: 'create' | 'edit', lessonId: string): void {
    const validation = this.materialsApi.validateFile(file);
    if (!validation.valid) {
      this.errorMessage = validation.error || 'Invalid file.';
      this.successMessage = '';
      return;
    }

    const progressKey = `${mode}-${lessonId}`;
    const material = mode === 'create' ? this.newMaterials[lessonId] : this.editMaterial;

    if (!material) {
      return;
    }

    // Create a temporary material to get upload feedback
    const tempMaterial: MaterialCreateRequest = {
      lessonId,
      type: material.type,
      title: material.title || file.name,
      url: file.name,
      content: material.content
    };

    // Create material first, then upload file to it
    this.uploading[progressKey] = true;
    this.uploadStatus[progressKey] = 'Preparing upload...';
    this.uploadProgress[progressKey] = 0;
    this.errorMessage = '';
    this.successMessage = '';
    this.materialsApi.createMaterial(tempMaterial).subscribe({
      next: (createdMaterial) => {
        // Now upload the file to the material
        this.materialsApi.uploadMaterialFile(createdMaterial.id, file).subscribe({
          next: (event) => {
            if (event && typeof event === 'object' && 'body' in event) {
              const updatedMaterial = (event as { body: MaterialResponse }).body;
              if (!updatedMaterial) {
                return;
              }
              material.url = updatedMaterial.url || updatedMaterial.fileName || file.name;
              this.uploadProgress[progressKey] = 0;
              this.uploadStatus[progressKey] = 'Upload complete.';
              this.uploading[progressKey] = false;
              this.setSuccessMessage('File uploaded successfully.');
              this.loadCourse(this.courseId);
            } else {
              // Progress event
              const progress = event as FileUploadProgress;
              this.uploadProgress[progressKey] = progress.percent;
              this.uploadStatus[progressKey] = `Uploading... ${progress.percent}%`;
            }
          },
          error: () => {
            this.errorMessage = 'File upload failed.';
            this.successMessage = '';
            this.uploadProgress[progressKey] = 0;
            this.uploadStatus[progressKey] = 'Upload failed.';
            this.uploading[progressKey] = false;
          }
        });
      },
      error: () => {
        this.errorMessage = 'Unable to create material.';
        this.successMessage = '';
        this.uploadStatus[progressKey] = 'Upload failed.';
        this.uploading[progressKey] = false;
      }
    });
  }

  cancelCreateMaterial(lessonId: string): void {
    delete this.newMaterials[lessonId];
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private coursesApi: CourseApiService,
    private chaptersApi: ChapterApiService,
    private lessonsApi: LessonApiService,
    private materialsApi: MaterialApiService,
    private enrollmentsApi: EnrollmentApiService,
    private certificates: CertificateService,
    private quizAttempts: QuizAttemptService,
    private quizzes: QuizService,
    private quizValidator: QuizValidationService,
    private auth: AuthService
  ) {
    const paramStreams = this.route.pathFromRoot.map(activeRoute => activeRoute.paramMap);
    combineLatest(paramStreams).pipe(
      map(paramMaps => {
        for (let i = paramMaps.length - 1; i >= 0; i--) {
          const id = paramMaps[i].get('courseId');
          if (id) {
            return id;
          }
        }
        return null;
      }),
      distinctUntilChanged()
    ).subscribe(id => {
      if (!id) {
        this.loading = false;
        this.errorMessage = 'Course not found.';
        return;
      }

      this.courseId = id;
      this.loading = true;
      this.errorMessage = '';

      if (!this.auth.isAuthenticated()) {
        this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
        return;
      }

      this.loadCourse(id);
    });
  }

  setTab(tab: Tab) { this.activeTab = tab; }
  isExpanded(id: string): boolean { return this.expandedChapters.includes(id); }
  toggleChapter(id: string) {
    this.expandedChapters = this.isExpanded(id)
      ? this.expandedChapters.filter(cid => cid !== id)
      : [...this.expandedChapters, id];
  }

  getSectionProgress(chapter: any): string {
    if (!chapter.sections || chapter.sections.length === 0) {
      return '0/0 completed';
    }
    const completed = chapter.sections.filter((s: any) => s.completed).length;
    const total = chapter.sections.length;
    return `${completed}/${total} completed`;
  }

  downloadCertificate(): void {
    if (!this.certificateAvailable) {
      return;
    }
    this.certificates.downloadCertificate(this.courseId);
  }

  private latestAttempts: QuizAttempt[] = [];

  isManager(): boolean {
    return this.auth.isAdmin() || this.auth.isTeacher();
  }

  isStudent(): boolean {
    return this.auth.isStudent();
  }

  canAccessContent(): boolean {
    if (!this.isStudent()) {
      return true;
    }
    if (!this.courseHasContent) {
      return false;
    }
    return !!this.enrollment && this.enrollment.status === 'ACTIVE';
  }

  requestEnrollment(): void {
    if (this.enrolling || !this.courseId) {
      return;
    }
    if (this.isPaidCourse) {
      this.router.navigate(['/payment'], {
        queryParams: {
          courseId: this.courseId,
          title: this.courseTitle,
          amount: this.coursePrice,
          returnUrl: `/courses/${this.courseId}`
        }
      });
      return;
    }
    this.enrolling = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.enrollmentsApi.requestEnrollment(this.courseId).subscribe({
      next: enrollment => {
        this.enrollment = enrollment;
        this.enrolling = false;
        this.setSuccessMessage('Enrollment successful. You can start your lessons below.');
        this.loadCourse(this.courseId);
      },
      error: () => {
        this.enrolling = false;
        this.errorMessage = 'Unable to enroll right now.';
      }
    });
  }

  completeLesson(lessonId: string): void {
    if (!this.canAccessContent()) {
      return;
    }
    this.enrollmentsApi.completeLesson(this.courseId, lessonId).subscribe({
      next: progress => {
        this.progress = progress;
        this.loadCourse(this.courseId);
      },
      error: () => {
        this.errorMessage = 'Unable to complete lesson.';
      }
    });
  }

  startLesson(lessonId: string): void {
    if (!this.canAccessContent()) {
      this.errorMessage = 'Enroll to access lesson content.';
      return;
    }
    this.router.navigate(['/lessons', lessonId]);
  }

  playCourse(): void {
    if (!this.canAccessContent()) {
      this.errorMessage = 'Enroll to access lesson content.';
      return;
    }
    const nextLessonId = this.progress?.nextLessonId;
    const firstLessonId = this.lessonsData[0]?.id;
    const targetLessonId = nextLessonId || firstLessonId;
    if (!targetLessonId) {
      this.errorMessage = 'No lesson available to play.';
      return;
    }
    this.router.navigate(['/lessons', targetLessonId], { queryParams: { autoplay: '1' } });
  }

  startCreateChapter(): void {
    this.creatingChapter = true;
    this.newChapter = { courseId: this.courseId, title: '', orderIndex: 1 };
  }

  cancelCreateChapter(): void {
    this.creatingChapter = false;
  }

  submitCreateChapter(): void {
    if (!this.newChapter.title.trim()) {
      this.errorMessage = 'Chapter title is required.';
      return;
    }
    this.errorMessage = '';
    this.successMessage = '';
    this.chaptersApi.createChapter(this.newChapter).subscribe({
      next: () => {
        this.creatingChapter = false;
        this.setSuccessMessage('Chapter created successfully.');
        this.loadCourse(this.courseId);
      },
      error: () => {
        this.errorMessage = 'Unable to create chapter.';
      }
    });
  }

  startEditChapter(chapter: ChapterResponse): void {
    this.editingChapterId = chapter.id;
    this.editChapter = { title: chapter.title, orderIndex: chapter.orderIndex };
  }

  cancelEditChapter(): void {
    this.editingChapterId = null;
  }

  submitEditChapter(chapterId: string): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.chaptersApi.updateChapter(chapterId, this.editChapter).subscribe({
      next: () => {
        this.editingChapterId = null;
        this.setSuccessMessage('Chapter updated successfully.');
        this.loadCourse(this.courseId);
      },
      error: () => {
        this.errorMessage = 'Unable to update chapter.';
      }
    });
  }

  deleteChapter(chapterId: string): void {
    if (!confirm('Delete this chapter?')) {
      return;
    }
    this.errorMessage = '';
    this.successMessage = '';
    this.chaptersApi.deleteChapter(chapterId).subscribe({
      next: () => {
        this.setSuccessMessage('Chapter deleted successfully.');
        this.loadCourse(this.courseId);
      },
      error: () => {
        this.errorMessage = 'Unable to delete chapter.';
      }
    });
  }

  initLessonForm(chapterId: string): void {
    if (!this.newLessons[chapterId]) {
      this.newLessons[chapterId] = { chapterId, title: '', orderIndex: 1, xpReward: 1 };
    }
  }

  submitCreateLesson(chapterId: string): void {
    const request = this.newLessons[chapterId];
    if (!request || !request.title.trim()) {
      this.errorMessage = 'Lesson title is required.';
      return;
    }
    this.errorMessage = '';
    this.successMessage = '';
    this.lessonsApi.createLesson(request).subscribe({
      next: () => {
        this.setSuccessMessage('Lesson created successfully.');
        this.loadCourse(this.courseId);
      },
      error: () => {
        this.errorMessage = 'Unable to create lesson.';
      }
    });
  }

  startEditLesson(lesson: LessonResponse): void {
    this.editingLessonId = lesson.id;
    this.editLesson = { title: lesson.title, orderIndex: lesson.orderIndex, xpReward: lesson.xpReward };
  }

  cancelEditLesson(): void {
    this.editingLessonId = null;
  }

  submitEditLesson(lessonId: string): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.lessonsApi.updateLesson(lessonId, this.editLesson).subscribe({
      next: () => {
        this.editingLessonId = null;
        this.setSuccessMessage('Lesson updated successfully.');
        this.loadCourse(this.courseId);
      },
      error: () => {
        this.errorMessage = 'Unable to update lesson.';
      }
    });
  }

  deleteLesson(lessonId: string): void {
    if (!confirm('Delete this lesson?')) {
      return;
    }
    this.errorMessage = '';
    this.successMessage = '';
    this.lessonsApi.deleteLesson(lessonId).subscribe({
      next: () => {
        this.setSuccessMessage('Lesson deleted successfully.');
        this.loadCourse(this.courseId);
      },
      error: () => {
        this.errorMessage = 'Unable to delete lesson.';
      }
    });
  }

  initMaterialForm(lessonId: string): void {
    if (!this.newMaterials[lessonId]) {
      this.newMaterials[lessonId] = { lessonId, type: 'TEXT', title: '', url: '', content: '' };
    }
  }

  submitCreateMaterial(lessonId: string): void {
    const request = this.newMaterials[lessonId];
    if (!request || !request.title.trim()) {
      this.errorMessage = 'Material title is required.';
      return;
    }
    this.errorMessage = '';
    this.successMessage = '';
    this.materialsApi.createMaterial(request).subscribe({
      next: () => {
        this.setSuccessMessage('Material created successfully.');
        this.loadCourse(this.courseId);
      },
      error: () => {
        this.errorMessage = 'Unable to create material.';
      }
    });
  }

  startEditMaterial(material: MaterialResponse): void {
    this.editingMaterialId = material.id;
    this.editMaterial = {
      type: material.type,
      title: material.title,
      url: material.url ?? '',
      content: material.content ?? ''
    };
  }

  cancelEditMaterial(): void {
    this.editingMaterialId = null;
  }

  submitEditMaterial(materialId: string): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.materialsApi.updateMaterial(materialId, this.editMaterial).subscribe({
      next: () => {
        this.editingMaterialId = null;
        this.setSuccessMessage('Material updated successfully.');
        this.loadCourse(this.courseId);
      },
      error: () => {
        this.errorMessage = 'Unable to update material.';
      }
    });
  }

  deleteMaterial(materialId: string): void {
    if (!confirm('Delete this material?')) {
      return;
    }
    this.errorMessage = '';
    this.successMessage = '';
    this.materialsApi.deleteMaterial(materialId).subscribe({
      next: () => {
        this.setSuccessMessage('Material deleted successfully.');
        this.loadCourse(this.courseId);
      },
      error: () => {
        this.errorMessage = 'Unable to delete material.';
      }
    });
  }

  initQuizForm(lessonId: string): void {
    const existing = this.quizByLesson[lessonId];
    if (existing) {
      this.editingQuizLessonId = lessonId;
      this.newQuiz[lessonId] = { ...existing };
      return;
    }

    this.newQuiz[lessonId] = this.quizzes.createQuiz(
      this.courseId,
      lessonId,
      'Lesson Quiz',
      15,
      70,
      [this.createEmptyQuestion()]
    );
  }

  addQuizQuestion(lessonId: string): void {
    const quiz = this.newQuiz[lessonId];
    if (!quiz) {
      return;
    }
    quiz.questions.push(this.createEmptyQuestion());
  }

  removeQuizQuestion(lessonId: string, index: number): void {
    const quiz = this.newQuiz[lessonId];
    if (!quiz || quiz.questions.length <= 1) {
      return;
    }
    quiz.questions.splice(index, 1);
  }

  saveQuiz(lessonId: string): void {
    const quiz = this.newQuiz[lessonId];
    if (!quiz || !quiz.title.trim() || quiz.questions.some(q => !q.text.trim() || q.options.some(opt => !opt.trim()))) {
      this.errorMessage = 'Fill in quiz title, questions, and options.';
      return;
    }
    quiz.updatedAt = new Date().toISOString();
    this.quizzes.saveQuiz(quiz).subscribe({
      next: () => {
        this.editingQuizLessonId = null;
        this.loadCourse(this.courseId);
      },
      error: () => {
        this.errorMessage = 'Unable to save quiz.';
      }
    });
  }

  deleteQuiz(lessonId: string): void {
    if (!confirm('Delete this quiz?')) {
      return;
    }
    const existing = this.quizByLesson[lessonId];
    if (!existing) {
      return;
    }
    this.quizzes.deleteQuiz(existing.id).subscribe({
      next: () => {
        this.editingQuizLessonId = null;
        this.loadCourse(this.courseId);
      },
      error: () => {
        this.errorMessage = 'Unable to delete quiz.';
      }
    });
  }

  cancelQuizEdit(): void {
    this.editingQuizLessonId = null;
  }

  isEditingMaterialForLesson(lessonId: string): boolean {
    if (!this.editingMaterialId) {
      return false;
    }
    return (this.materialsByLesson[lessonId] ?? []).some(material => material.id === this.editingMaterialId);
  }

  isQuizCompleted(lessonId?: string): boolean {
    if (!lessonId) {
      return false;
    }
    return this.latestAttempts.some(attempt => attempt.lessonId === lessonId || attempt.quizId === lessonId);
  }

  private loadCourse(courseId: string): void {
    this.loading = true;
    this.errorMessage = '';

    const enrollment$ = this.enrollmentsApi.getEnrollment(courseId).pipe(catchError(() => of(null)));

    const progress$ = enrollment$.pipe(
      switchMap(enrollment => {
        if (enrollment && (enrollment.status === 'ACTIVE' || enrollment.status === 'COMPLETED')) {
          return this.enrollmentsApi.getProgress(courseId).pipe(catchError(() => of(null)));
        }
        return of(null);
      })
    );

    forkJoin({
      course: this.coursesApi.getCourse(courseId),
      chapters: this.chaptersApi.listChapters(courseId),
      lessons: this.lessonsApi.listLessonsByCourse(courseId),
      progress: progress$,
      enrollment: enrollment$
    }).pipe(
      switchMap(result => {
        const sortedLessons = [...result.lessons].sort((a, b) => a.orderIndex - b.orderIndex);
        if (sortedLessons.length === 0) {
          return of({
            courseTitle: result.course.title,
            courseImageUrl: this.resolveImageUrl(result.course.imageUrl) ?? '',
            coursePrice: result.course.price ?? 0,
            isPaidCourse: !!result.course.isPaid,
            chapters: result.chapters,
            lessons: sortedLessons,
            lessonMaterials: [] as MaterialResponse[][],
            lessonQuizzes: [] as (Quiz | null)[],
            progress: result.progress,
            enrollment: result.enrollment
          });
        }

        const materialRequests = sortedLessons.map(lesson =>
          this.materialsApi.listMaterials(lesson.id).pipe(catchError(() => of([])))
        );

        const quizRequests = sortedLessons.map(lesson =>
          this.quizzes.getQuizByLesson(lesson.id).pipe(catchError(() => of(null)))
        );

        return forkJoin({
          lessonMaterials: forkJoin(materialRequests),
          lessonQuizzes: forkJoin(quizRequests)
        }).pipe(
          map(({ lessonMaterials, lessonQuizzes }) => ({
            courseTitle: result.course.title,
            courseImageUrl: this.resolveImageUrl(result.course.imageUrl) ?? '',
            coursePrice: result.course.price ?? 0,
            isPaidCourse: !!result.course.isPaid,
            chapters: result.chapters,
            lessons: sortedLessons,
            lessonMaterials,
            lessonQuizzes,
            progress: result.progress,
            enrollment: result.enrollment
          }))
        );
      })
    ).subscribe({
      next: result => {
        this.courseTitle = result.courseTitle;
        this.courseImageUrl = result.courseImageUrl;
        this.coursePrice = result.coursePrice;
        this.isPaidCourse = result.isPaidCourse;
        this.progress = result.progress;
        this.enrollment = result.enrollment;
        this.chaptersData = result.chapters;
        this.lessonsData = result.lessons;
        this.materialsByLesson = {};
        this.quizByLesson = {};
        result.lessons.forEach((lesson, index) => {
          this.materialsByLesson[lesson.id] = result.lessonMaterials[index] ?? [];
        });
        result.lessons.forEach((lesson, index) => {
          const quiz = result.lessonQuizzes[index];
          if (quiz) {
            this.quizByLesson[lesson.id] = quiz;
          }
        });
        const hasMaterialContent = result.lessonMaterials.some(list => (list ?? []).length > 0);
        const hasQuizContent = Object.keys(this.quizByLesson).length > 0;
        const hasLessons = result.lessons.length > 0;
        this.courseHasContent = hasLessons || hasMaterialContent || hasQuizContent;
        this.creatingChapter = false;
        this.editingChapterId = null;
        this.editingLessonId = null;
        this.editingMaterialId = null;
        this.chapters = this.buildChapterViews(result.chapters, result.lessons, result.lessonMaterials, result.progress);
        this.expandedChapters = this.chapters.length ? [this.chapters[0].id] : [];
        this.loading = false;
        
        if (this.isStudent()) {
          this.refreshCertificateAvailability(courseId, result.progress);
        }
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Unable to load course details.';
      }
    });
  }

  private buildChapterViews(
    chapters: ChapterResponse[],
    lessons: LessonResponse[],
    lessonMaterials: MaterialResponse[][],
    progress: ProgressResponse | null
  ): ChapterView[] {
    const sortedChapters = [...chapters].sort((a, b) => a.orderIndex - b.orderIndex);
    const completedLessons = progress?.completedLessons ?? 0;
    const completedIds = new Set(lessons.slice(0, completedLessons).map(lesson => lesson.id));

    const lessonMaterialsMap = new Map<string, MaterialResponse[]>();
    lessons.forEach((lesson, index) => {
      lessonMaterialsMap.set(lesson.id, lessonMaterials[index] ?? []);
    });

    return sortedChapters.map(chapter => {
      const chapterLessons = lessons.filter(lesson => lesson.chapterId === chapter.id);
      const sections: SectionView[] = [];

      chapterLessons.forEach(lesson => {
        sections.push({
          id: lesson.id,
          title: lesson.title,
          duration: `${lesson.xpReward} XP`,
          completed: completedIds.has(lesson.id),
          lessonId: lesson.id,
          type: 'LESSON'
        });

        const materials = lessonMaterialsMap.get(lesson.id) ?? [];
        const hasQuiz = materials.some(material => material.type === 'QUIZ') || !!this.quizByLesson[lesson.id];
        if (hasQuiz) {
          sections.push({
            id: `${lesson.id}-quiz`,
            title: 'Quiz',
            duration: 'Quiz',
            completed: false,
            quizLessonId: lesson.id,
            type: 'QUIZ'
          });
        }
      });

      return {
        id: chapter.id,
        title: chapter.title,
        sections
      };
    });
  }

  private refreshCertificateAvailability(courseId: string, progress: ProgressResponse | null): void {
    this.quizAttempts.getAttempts(courseId).subscribe(attempts => {
      this.latestAttempts = attempts;
      this.certificateAvailable = this.certificates.canDownloadCertificate(progress, attempts, this.passingScore);
    });
  }

  private createEmptyQuestion(): QuizQuestion {
    return {
      id: `q-${Math.random().toString(36).slice(2, 8)}`,
      text: '',
      type: 'MULTIPLE_CHOICE',
      options: ['Option 1', 'Option 2', 'Option 3', 'Option 4'],
      correctIndex: 0
    };
  }

  materialFileUrl(materialId: string): string {
    return `${COURSE_API_BASE_URL}/materials/${materialId}/file`;
  }

  onCourseImageError(): void {
    this.courseImageUrl = '';
  }

  private resolveImageUrl(value: string | null | undefined): string | undefined {
    if (!value) {
      return undefined;
    }
    const trimmed = value.trim();
    if (!trimmed) {
      return undefined;
    }
    if (trimmed.startsWith('data:image/') || trimmed.startsWith('http://') || trimmed.startsWith('https://')) {
      return trimmed;
    }
    if (trimmed.startsWith('/')) {
      return `${COURSE_API_BASE_URL}${trimmed}`;
    }
    return `${COURSE_API_BASE_URL}/${trimmed}`;
  }

  private setSuccessMessage(message: string): void {
    this.successMessage = message;
    setTimeout(() => {
      if (this.successMessage === message) {
        this.successMessage = '';
      }
    }, 3500);
  }
}
