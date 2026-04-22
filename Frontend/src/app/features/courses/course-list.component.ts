import { Component, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { trigger, transition, style, animate, state } from '@angular/animations';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { CourseApiService } from '../../services/course-api.service';
import { EnrollmentApiService } from '../../services/enrollment-api.service';
import { CourseDisplayService } from '../../services/course-display.service';
import { AuthService } from '../../services/auth.service';
import { ProgressTrackingService, StudentProgress } from '../../services/progress-tracking.service';
import { NotificationService } from '../../services/notification.service';
import { CourseCreateRequest, CourseResponse, CourseReviewSummaryResponse, CourseUpdateRequest, EnrollmentResponse, Level, Role } from '../../models/api-models';
import { DEFAULT_COURSE_IMAGE, DEFAULT_COURSE_PRICE } from '../../data/course-display.config';
import { Router } from '@angular/router';
import { COURSE_API_BASE_URL } from '../../services/api.config';
import { EnrollmentCourse, EnrollmentModalComponent } from '../../shared/enrollment-modal/enrollment-modal.component';

interface CourseCard {
  id: string;
  title: string;
  description: string;
  level: string;
  capacity: number;
  active: boolean;
  hasContent: boolean;
  image: string;
  price: number;
  isPaid: boolean;
  badge: 'FREE' | 'PAID';
  enrolled: boolean;
  progressPercent?: number;
  status?: string;
  currentLessonId?: string;
  enrollmentError?: string;
  reviewCount: number;
  averageRating: number;
}

@Component({
  selector: 'app-course-list',
  templateUrl: './course-list.component.html',
  styleUrls: ['./course-list.component.css'],
  animations: [
    trigger('fadeInOut', [
      transition(':enter', [
        style({ opacity: 0, transform: 'scale(0.95)' }),
        animate('400ms cubic-bezier(0.4, 0, 0.2, 1)', style({ opacity: 1, transform: 'scale(1)' }))
      ]),
      transition(':leave', [
        animate('400ms cubic-bezier(0.4, 0, 0.2, 1)', style({ opacity: 0, transform: 'scale(1.05)' }))
      ])
    ]),
    trigger('slideDown', [
      transition(':enter', [
        style({ height: 0, opacity: 0, overflow: 'hidden' }),
        animate('300ms ease-out', style({ height: '*', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('300ms ease-in', style({ height: 0, opacity: 0, overflow: 'hidden' }))
      ])
    ]),
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('300ms ease-out', style({ opacity: 1 }))
      ])
    ]),
    trigger('cardSlideIn', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(20px)' }),
        animate('{{ delay }}ms 300ms cubic-bezier(0.4, 0, 0.2, 1)', style({ opacity: 1, transform: 'translateY(0)' })),
      ], { params: { delay: 0 } })
    ]),
    trigger('bounce', [
      transition(':enter', [
        animate('600ms cubic-bezier(0.34, 1.56, 0.64, 1)',
          style({ transform: '*' })
        )
      ])
    ]),
    trigger('pulse', [
      state('active', style({ opacity: '0.7' })),
      state('inactive', style({ opacity: '1' })),
      transition('inactive <=> active', animate('1500ms ease-in-out'))
    ])
  ]
})
export class CourseListComponent implements OnInit, OnDestroy {
  showQuoteOverlay = true;
  currentQuote = '';
  quotes = [
    "The beautiful thing about learning is that no one can take it away from you. – B.B. King",
    "Education is the most powerful weapon which you can use to change the world. – Nelson Mandela",
    "Live as if you were to die tomorrow. Learn as if you were to live forever. – Mahatma Gandhi",
    "The mind is not a vessel to be filled, but a fire to be kindled. – Plutarch",
    "Do not wait to strike till the iron is hot, but make it hot by striking. – William Butler Yeats",
    "Teachers can open the door, but you must enter it yourself. – Chinese Proverb"
  ];
  showStudentChatbot = false;
  courses: CourseCard[] = [];
  filteredCourses: CourseCard[] = [];
  categories: string[] = ['All', 'Programming', 'Design', 'Business', 'Marketing', 'Science'];
  loading = true;
  errorMessage = '';
  successMessage = '';
  actionPending = new Set<string>();
  enrollmentErrors: Record<string, string> = {};
  levels: Level[] = ['A1', 'A2', 'B1', 'B2', 'C1', 'C2'];
  creating = false;
  editingCourseId: string | null = null;
  deletingCourseId: string | null = null;
  createSubmitted = false;
  editSubmitted = false;

  // Search and filter
  searchQuery = '';
  selectedLevel: Level | 'ALL' = 'ALL';
  selectedStatus: 'ALL' | 'ACTIVE' | 'INACTIVE' = 'ALL';
  selectedType: 'ALL' | 'FREE' | 'PAID' = 'ALL';

  // Student list pagination
  currentPage = 1;
  itemsPerPage = 6;

  // File upload
  dragOver = false;
  uploadProgress: Record<string, number> = {};
  uploadStatus: Record<string, string> = {};
  uploading: Record<string, boolean> = {};
  imageErrors: Record<string, number> = {};
  createStatus = '';
  editStatus = '';
  savingCreate = false;
  savingEdit = false;

  newCourse: CourseCreateRequest = {
    title: '',
    description: '',
    level: 'A1',
    capacity: 1,
    active: true,
    isPaid: false,
    price: 0
  };
  newCoursePrice = 0;
  newCoursePaid = false;
  newCourseImage = '';

  editCourse: CourseUpdateRequest = {
    title: '',
    description: '',
    level: 'A1',
    capacity: 1,
    active: true,
    isPaid: false,
    price: 0
  };
  editCoursePrice = 0;
  editCoursePaid = false;
  editCourseImage = '';

  // Enrollment Modal
  @ViewChild(EnrollmentModalComponent) enrollmentModal?: EnrollmentModalComponent;
  showEnrollmentModal = false;
  enrollmentCourse: EnrollmentCourse | null = null;

  constructor(
    private coursesApi: CourseApiService,
    private enrollments: EnrollmentApiService,
    private display: CourseDisplayService,
    private auth: AuthService,
    private router: Router,
    private progressTracking: ProgressTrackingService,
    private notification: NotificationService
  ) { }

  ngOnInit(): void {
    this.currentQuote = this.quotes[Math.floor(Math.random() * this.quotes.length)];
    setTimeout(() => {
      this.showQuoteOverlay = false;
    }, 6000);

    this.loadCourses();
  }

  closeQuoteOverlay(): void {
    this.showQuoteOverlay = false;
  }

  get totalPages(): number {
    const totalItems = this.filteredCourses.length;
    return totalItems === 0 ? 1 : Math.ceil(totalItems / this.itemsPerPage);
  }

  get paginatedCourses(): CourseCard[] {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return this.filteredCourses.slice(start, end);
  }

  get pageStartItem(): number {
    if (this.filteredCourses.length === 0) {
      return 0;
    }
    return (this.currentPage - 1) * this.itemsPerPage + 1;
  }

  get pageEndItem(): number {
    return Math.min(this.currentPage * this.itemsPerPage, this.filteredCourses.length);
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, index) => index + 1);
  }

  goToPage(page: number): void {
    const safePage = Math.max(1, Math.min(page, this.totalPages));
    this.currentPage = safePage;
  }

  nextPage(): void {
    this.goToPage(this.currentPage + 1);
  }

  prevPage(): void {
    this.goToPage(this.currentPage - 1);
  }

  applyFilters(): void {
    this.filteredCourses = this.courses.filter(course => {
      // Search filter
      const searchMatch = !this.searchQuery.trim() ||
        course.title.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        course.description.toLowerCase().includes(this.searchQuery.toLowerCase());

      // Level filter
      const levelMatch = this.selectedLevel === 'ALL' || course.level === this.selectedLevel;

      // Status filter
      const statusMatch = this.selectedStatus === 'ALL' ||
        (this.selectedStatus === 'ACTIVE' && course.active) ||
        (this.selectedStatus === 'INACTIVE' && !course.active);

      // Type filter
      const typeMatch = this.selectedType === 'ALL' ||
        (this.selectedType === 'FREE' && !course.isPaid) ||
        (this.selectedType === 'PAID' && course.isPaid);

      return searchMatch && levelMatch && statusMatch && typeMatch;
    });

    this.goToPage(1);
  }

  resetFilters(): void {
    this.searchQuery = '';
    this.selectedLevel = 'ALL';
    this.selectedStatus = 'ALL';
    this.selectedType = 'ALL';
    this.applyFilters();
  }

  onImageSelected(event: Event, mode: 'create' | 'edit'): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      this.handleImageUpload(file, mode);
    }
  }

  onImageDrop(event: DragEvent, mode: 'create' | 'edit'): void {
    event.preventDefault();
    this.dragOver = false;
    const file = event.dataTransfer?.files?.[0];
    if (file && file.type.startsWith('image/')) {
      this.handleImageUpload(file, mode);
    }
  }

  private handleImageUpload(file: File, mode: 'create' | 'edit'): void {
    // Validate file type
    if (!file.type.startsWith('image/')) {
      this.errorMessage = 'Please upload a valid image file.';
      return;
    }

    // Validate file size (max 5MB for images)
    const maxBytes = 5 * 1024 * 1024;
    if (file.size > maxBytes) {
      this.errorMessage = 'Image size must be under 5MB.';
      return;
    }

    const reader = new FileReader();
    this.uploading[mode] = true;
    this.uploadStatus[mode] = 'Preparing image...';
    this.uploadProgress[mode] = 0;
    reader.onprogress = (event) => {
      if (event.lengthComputable) {
        const percent = Math.round((event.loaded / event.total) * 100);
        this.uploadProgress[mode] = percent;
        this.uploadStatus[mode] = `Uploading... ${percent}%`;
      }
    };
    reader.onload = (e) => {
      const dataUrl = e.target?.result as string;
      if (mode === 'create') {
        this.newCourseImage = dataUrl;
      } else {
        this.editCourseImage = dataUrl;
      }
      this.uploadProgress[mode] = 100;
      this.uploadStatus[mode] = 'Image ready.';
      this.uploading[mode] = false;
      this.setSuccessMessage('Image ready to save.');
    };
    reader.onerror = () => {
      this.errorMessage = 'Failed to read image file.';
      this.uploadStatus[mode] = 'Upload failed.';
      this.uploading[mode] = false;
    };
    reader.readAsDataURL(file);
  }

  loadCourses(): void {
    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      courses: this.coursesApi.listCourses(0, 50).pipe(
        map(page => page.content ?? []),
        catchError(() => {
          this.errorMessage = 'Unable to load courses right now.';
          return of([] as CourseResponse[]);
        })
      ),
      allProgress: this.progressTracking.getAllProgress().pipe(catchError(() => of([])))
    }).pipe(
      switchMap(({ courses, allProgress }) => {
        // Create a progress map for quick lookup
        const progressMap = new Map<string, StudentProgress>();
        allProgress.forEach(p => progressMap.set(p.courseId, p));

        return forkJoin({
          enrollments: this.attachEnrollments(courses),
          reviewSummaries: this.loadReviewSummaries(courses)
        }).pipe(
          map(({ enrollments, reviewSummaries }) => enrollments.map(item =>
            this.toCard(
              item.course,
              item.enrollment,
              item.index,
              progressMap.get(item.course.id),
              reviewSummaries.get(item.course.id)
            )
          ))
        );
      })
    ).subscribe({
      next: cards => {
        this.courses = cards;
        this.applyFilters();
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load courses right now.';
        this.loading = false;
      }
    });
  }

  handleAction(card: CourseCard): void {
    // If manager, just navigate to the tutor content studio
    if (this.auth.isTutor() || this.auth.isAdmin()) {
      console.log('Manager navigating to course:', card.id);
      this.router.navigate(['/training-details', card.id]);
      return;
    }

    // If course is not active, just view details
    if (!card.active) {
      this.router.navigate(['/courses', card.id]);
      return;
    }

    // If student already enrolled, check for resume capability
    if (card.enrolled) {
      // If there's a current lesson saved, navigate directly to it (resume)
      if (card.currentLessonId) {
        console.log('Student resuming lesson:', card.currentLessonId);
        this.router.navigate(['/lessons', card.currentLessonId]);
        return;
      }

      // Otherwise, go to course details
      console.log('Student continuing in course:', card.id);
      this.router.navigate(['/courses', card.id]);
      return;
    }

    // Prevent double-click
    if (this.actionPending.has(card.id)) {
      console.log('Enrollment already in progress for:', card.id);
      return;
    }

    // Show enrollment modal for enrollment (allow even without content)
    this.openEnrollmentModal(card);
  }

  openEnrollmentModal(card: CourseCard): void {
    this.enrollmentCourse = {
      id: card.id,
      title: card.title,
      description: card.description,
      level: card.level,
      image: card.image,
      isPaid: card.isPaid,
      price: card.price
    };
    this.showEnrollmentModal = true;
  }

  closeEnrollmentModal(): void {
    this.showEnrollmentModal = false;
    this.enrollmentCourse = null;
  }

  confirmEnrollment(): void {
    if (!this.enrollmentCourse) return;

    const courseId = this.enrollmentCourse.id;
    const courseTitle = this.enrollmentCourse.title;

    // Redirect immediately so students always enter the selected course page.
    this.closeEnrollmentModal();
    this.router.navigate(['/courses', courseId]);

    if (this.enrollmentCourse.isPaid) {
      this.router.navigateByUrl(
        this.router.createUrlTree(['/payment'], {
          queryParams: {
            courseId,
            title: this.enrollmentCourse.title,
            amount: this.enrollmentCourse.price ?? 0,
            returnUrl: `/courses/${courseId}`
          }
        })
      );
      this.closeEnrollmentModal();
      return;
    }

    // Mark as pending
    this.actionPending.add(courseId);
    delete this.enrollmentErrors[courseId];
    console.log('Enrolling student in course:', courseId);

    // Update local state to show as enrolled immediately
    const cardIndex = this.courses.findIndex(c => c.id === courseId);
    if (cardIndex !== -1) {
      this.courses[cardIndex].enrolled = true;
      this.applyFilters();
    }

    // Try to call the backend enrollment endpoint
    this.enrollments.requestEnrollment(courseId).pipe(
      catchError((error) => {
        console.error('Enrollment error:', error);
        const message = error?.error?.message || 'Enrollment failed. Please try again.';
        this.enrollmentErrors[courseId] = message;
        this.actionPending.delete(courseId);
        // Revert optimistic enrollment when backend fails
        const revertIndex = this.courses.findIndex(c => c.id === courseId);
        if (revertIndex !== -1) {
          this.courses[revertIndex].enrolled = false;
          this.applyFilters();
        }
        this.closeEnrollmentModal();
        return of(null);
      })
    ).subscribe((enrollment) => {
      if (!enrollment) {
        return;
      }
      console.log('Enrollment completed for course:', courseId);
      this.actionPending.delete(courseId);

      // Show success notification
      this.notification.celebrate(
        '🎉 Enrollment Success!',
        `You're now enrolled in ${courseTitle || 'the course'}`,
        5000
      );
    });
  }

  onStartLearning(courseId?: string): void {
    const targetCourseId = courseId ?? this.enrollmentCourse?.id;
    if (!targetCourseId) {
      return;
    }

    this.closeEnrollmentModal();
    this.router.navigate(['/courses', targetCourseId]);
  }

  isActionBusy(courseId: string): boolean {
    return this.actionPending.has(courseId);
  }

  isManager(): boolean {
    return this.auth.isAdmin() || this.auth.isTeacher();
  }

  startCreate(): void {
    this.creating = true;
    this.editingCourseId = null;
    this.createSubmitted = false;
    this.newCourse = { title: '', description: '', level: 'A1', capacity: 1, active: true };
    this.newCoursePrice = 0;
    this.newCoursePaid = false;
    this.newCourseImage = '';
  }

  cancelCreate(): void {
    this.creating = false;
  }

  onNewCourseImageSelected(event: Event): void {
    this.onImageSelected(event, 'create');
  }

  clearNewCourseImage(): void {
    this.newCourseImage = '';
    this.uploadStatus.create = '';
    this.uploadProgress.create = 0;
    this.uploading.create = false;
  }

  onEditCourseImageSelected(event: Event): void {
    this.onImageSelected(event, 'edit');
  }

  clearEditCourseImage(): void {
    this.editCourseImage = '';
    this.uploadStatus.edit = '';
    this.uploadProgress.edit = 0;
    this.uploading.edit = false;
  }

  submitCreate(): void {
    this.createSubmitted = true;
    if (!this.isCreateValid()) {
      this.errorMessage = 'Please fix the highlighted fields.';
      return;
    }
    this.errorMessage = '';
    this.successMessage = '';
    this.savingCreate = true;
    this.createStatus = 'Creating course...';

    const payload: CourseCreateRequest = {
      ...this.newCourse,
      isPaid: this.newCoursePaid,
      price: this.newCoursePrice,
      imageUrl: this.normalizeImageUrl(this.newCourseImage)
    };

    const tempId = `temp-${Date.now()}`;
    const optimisticCard: CourseCard = {
      id: tempId,
      title: payload.title,
      description: payload.description,
      level: payload.level,
      capacity: payload.capacity,
      active: payload.active ?? true,
      hasContent: false,
      image: this.newCourseImage || DEFAULT_COURSE_IMAGE,
      price: payload.price ?? 0,
      isPaid: payload.isPaid ?? false,
      badge: (payload.isPaid ?? false) ? 'PAID' : 'FREE',
      enrolled: false,
      progressPercent: 0,
      reviewCount: 0,
      averageRating: 0
    };

    this.courses = [optimisticCard, ...this.courses];
    this.applyFilters();

    this.coursesApi.createCourse(payload).subscribe({
      next: course => {
        this.courses = this.courses.filter(item => item.id !== tempId);
        this.display.setOverride(course.id, {
          price: this.newCoursePrice,
          isPaid: this.newCoursePaid
        });
        this.upsertCourseCard(course);
        this.applyFilters();
        this.creating = false;
        this.savingCreate = false;
        this.createStatus = '';
        this.setSuccessMessage('Course created successfully.');
      },
      error: () => {
        this.courses = this.courses.filter(item => item.id !== tempId);
        this.applyFilters();
        this.savingCreate = false;
        this.createStatus = '';
        this.errorMessage = 'Unable to create course.';
      }
    });
  }

  startEdit(course: CourseCard): void {
    this.creating = false;
    this.editingCourseId = course.id;
    this.editSubmitted = false;
    this.editCourse = {
      title: course.title,
      description: course.description,
      level: course.level as Level,
      capacity: course.capacity,
      active: course.active,
      isPaid: course.isPaid,
      price: course.price
    };
    this.editCoursePrice = course.price;
    this.editCoursePaid = course.isPaid;
    this.editCourseImage = course.image;
  }

  cancelEdit(): void {
    this.editingCourseId = null;
  }

  submitEdit(courseId: string): void {
    this.editSubmitted = true;
    if (!this.isEditValid()) {
      this.errorMessage = 'Please fix the highlighted fields.';
      return;
    }
    this.errorMessage = '';
    this.successMessage = '';
    this.savingEdit = true;
    this.editStatus = 'Saving changes...';
    const payload: CourseUpdateRequest = {
      ...this.editCourse,
      isPaid: this.editCoursePaid,
      price: this.editCoursePrice,
      imageUrl: this.normalizeImageUrl(this.editCourseImage)
    };

    const previousCard = this.courses.find(item => item.id === courseId);
    if (previousCard) {
      const optimisticCard: CourseCard = {
        ...previousCard,
        title: payload.title,
        description: payload.description,
        level: payload.level,
        capacity: payload.capacity,
        active: payload.active ?? previousCard.active,
        image: this.editCourseImage || previousCard.image,
        isPaid: payload.isPaid ?? previousCard.isPaid,
        price: payload.price ?? previousCard.price,
        badge: (payload.isPaid ?? previousCard.isPaid) ? 'PAID' : 'FREE',
        reviewCount: previousCard.reviewCount,
        averageRating: previousCard.averageRating
      };
      const optimisticCourses = [...this.courses];
      const index = optimisticCourses.findIndex(item => item.id === courseId);
      optimisticCourses[index] = optimisticCard;
      this.courses = optimisticCourses;
      this.applyFilters();
    }

    this.coursesApi.updateCourse(courseId, payload).subscribe({
      next: (updatedCourse) => {
        this.display.setOverride(courseId, {
          price: this.editCoursePrice,
          isPaid: this.editCoursePaid
        });
        this.upsertCourseCard(updatedCourse);
        this.applyFilters();
        this.editingCourseId = null;
        this.savingEdit = false;
        this.editStatus = '';
        this.setSuccessMessage('Course updated successfully.');
      },
      error: () => {
        if (previousCard) {
          const rollbackCourses = [...this.courses];
          const index = rollbackCourses.findIndex(item => item.id === courseId);
          if (index !== -1) {
            rollbackCourses[index] = previousCard;
            this.courses = rollbackCourses;
            this.applyFilters();
          }
        }
        this.savingEdit = false;
        this.editStatus = '';
        this.errorMessage = 'Unable to update course.';
      }
    });
  }

  deleteCourse(courseId: string): void {
    if (!confirm('Are you sure you want to delete this course? This action cannot be undone.')) {
      return;
    }
    this.errorMessage = '';
    this.successMessage = '';
    this.deletingCourseId = courseId;
    const previousCourses = [...this.courses];
    this.courses = this.courses.filter(course => course.id !== courseId);
    this.applyFilters();
    this.coursesApi.deleteCourse(courseId).subscribe({
      next: () => {
        this.display.removeOverride(courseId);
        this.deletingCourseId = null;
        this.errorMessage = '';
        this.setSuccessMessage('Course deleted successfully.');
      },
      error: () => {
        this.courses = previousCourses;
        this.applyFilters();
        this.deletingCourseId = null;
        this.errorMessage = 'Unable to delete course. Please try again.';
      }
    });
  }

  private upsertCourseCard(course: CourseResponse): void {
    const existingIndex = this.courses.findIndex(item => item.id === course.id);
    if (existingIndex === -1) {
      const newCard = this.toCard(course, null, this.courses.length, undefined);
      this.courses = [newCard, ...this.courses];
      return;
    }

    const existingCard = this.courses[existingIndex];
    const override = this.display.getOverride(course.id);
    const price = override.price ?? existingCard.price;
    const isPaid = override.isPaid ?? existingCard.isPaid;
    const image = this.resolveCourseImage(course, override.image ?? existingCard.image, existingIndex);
    const hasContent = (course.totalLessons ?? 0) > 0;

    const updatedCard: CourseCard = {
      ...existingCard,
      title: course.title,
      description: course.description,
      level: course.level,
      capacity: course.capacity,
      active: course.active,
      hasContent,
      image,
      price,
      isPaid,
      badge: isPaid ? 'PAID' : 'FREE'
    };

    const nextCourses = [...this.courses];
    nextCourses[existingIndex] = updatedCard;
    this.courses = nextCourses;
  }

  private attachEnrollments(courses: CourseResponse[]) {
    if (this.isManager() || courses.length === 0) {
      return of(courses.map((course, index) => ({ course, enrollment: null as EnrollmentResponse | null, index })));
    }

    const enrollmentRequests = courses.map(course =>
      this.enrollments.getEnrollment(course.id).pipe(catchError(() => of(null)))
    );

    return forkJoin(enrollmentRequests).pipe(
      map(enrollments =>
        courses.map((course, index) => ({
          course,
          enrollment: enrollments[index] ?? null,
          index
        }))
      )
    );
  }

  private toCard(
    course: CourseResponse,
    enrollment: EnrollmentResponse | null,
    index: number,
    progress?: StudentProgress,
    reviewSummary?: CourseReviewSummaryResponse
  ): CourseCard {
    const override = this.display.getOverride(course.id);
    const price = override.price ?? (course as any).price ?? 0;
    const isPaid = override.isPaid ?? (course as any).paid ?? (course as any).isPaid ?? (price > 0);
    const image = this.resolveCourseImage(course, override.image, index);
    const enrolled = enrollment?.status === 'ACTIVE';
    const hasContent = (course.totalLessons ?? 0) > 0;

    // Use progress from tracking service if available, otherwise fallback to enrollment
    const progressPercent = progress?.progressPercent ?? enrollment?.progressPercent ?? 0;
    const currentLessonId = progress?.currentLessonId ?? undefined;

    return {
      id: course.id,
      title: course.title,
      description: course.description,
      level: course.level,
      capacity: course.capacity,
      active: course.active,
      hasContent,
      image,
      price,
      isPaid,
      badge: isPaid ? 'PAID' : 'FREE',
      enrolled,
      progressPercent,
      status: enrollment?.status,
      currentLessonId,
      enrollmentError: this.enrollmentErrors[course.id],
      reviewCount: reviewSummary?.totalReviews ?? 0,
      averageRating: reviewSummary?.averageRating ?? 0
    };
  }

  private loadReviewSummaries(courses: CourseResponse[]): Observable<Map<string, CourseReviewSummaryResponse>> {
    if (courses.length === 0) {
      return of(new Map<string, CourseReviewSummaryResponse>());
    }

    return forkJoin(
      courses.map(course =>
        this.coursesApi.getCourseReviewSummary(course.id).pipe(
          catchError(() => of({
            courseId: course.id,
            averageRating: 0,
            totalReviews: 0
          } as CourseReviewSummaryResponse))
        )
      )
    ).pipe(
      map(summaries => new Map(summaries.map(summary => [summary.courseId, summary])))
    );
  }

  private resolveCourseImage(course: CourseResponse, overrideImage: string | undefined, index: number): string {
    const candidates = [course.imageUrl ?? undefined, overrideImage]
      .map(candidate => this.resolveImageUrl(candidate))
      .filter((candidate): candidate is string => !!candidate);
    const selected = candidates.find(candidate => this.isValidImageUrl(candidate));
    return selected ?? `${DEFAULT_COURSE_IMAGE}?course=${index + 1}`;
  }

  private isValidImageUrl(value: string | null | undefined): boolean {
    if (!value) {
      return false;
    }
    const trimmed = value.trim();
    if (!trimmed) {
      return false;
    }
    return trimmed.startsWith('data:image/') || trimmed.startsWith('http://') || trimmed.startsWith('https://');
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

  private normalizeImageUrl(value: string): string | undefined {
    const trimmed = value.trim();
    return trimmed ? trimmed : undefined;
  }

  onCourseImageError(courseId: string): void {
    const course = this.courses.find(item => item.id === courseId);
    if (!course) {
      return;
    }
    const errors = (this.imageErrors[courseId] ?? 0) + 1;
    this.imageErrors[courseId] = errors;
    if (errors === 1) {
      course.image = DEFAULT_COURSE_IMAGE;
    } else {
      course.image = '';
    }
    this.applyFilters();
  }

  private setSuccessMessage(message: string): void {
    this.successMessage = message;
    setTimeout(() => {
      if (this.successMessage === message) {
        this.successMessage = '';
      }
    }, 3500);
  }

  private isCreateValid(): boolean {
    return this.hasText(this.newCourse.title)
      && this.hasText(this.newCourse.description)
      && this.isPositiveInt(this.newCourse.capacity)
      && (!this.newCoursePaid || this.isNonNegativeNumber(this.newCoursePrice));
  }

  private isEditValid(): boolean {
    return this.hasText(this.editCourse.title)
      && this.hasText(this.editCourse.description)
      && this.isPositiveInt(this.editCourse.capacity)
      && (!this.editCoursePaid || this.isNonNegativeNumber(this.editCoursePrice));
  }

  private hasText(value: string): boolean {
    return value.trim().length > 0;
  }

  private isPositiveInt(value: number): boolean {
    return Number.isFinite(value) && value >= 1;
  }

  private isNonNegativeNumber(value: number): boolean {
    return Number.isFinite(value) && value >= 0;
  }

  ngOnDestroy(): void {
    this.creating = false;
    this.editingCourseId = null;
    this.deletingCourseId = null;
  }
}
