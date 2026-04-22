export type Role = 'ADMIN' | 'TUTOR' | 'TEACHER' | 'STUDENT' | 'USER';
export type Level = 'A1' | 'A2' | 'B1' | 'B2' | 'C1' | 'C2';
export type EnrollmentStatus = 'PENDING' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
export type MaterialType = 'VIDEO' | 'AUDIO' | 'TEXT' | 'QUIZ' | 'SLIDES' | 'LINK';

export interface Page<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CourseResponse {
  id: string;
  title: string;
  description: string;
  level: Level;
  capacity: number;
  active: boolean;
  isPaid: boolean;
  price: number;
  imageUrl?: string | null;
  totalChapters: number;
  totalLessons: number;
  createdAt: string;
  updatedAt: string;
  tutorId?: string;
  tutorName?: string;
  enrollmentCount?: number;
  isOwnedByCurrentUser?: boolean;
}

export interface CourseCreateRequest {
  title: string;
  description: string;
  level: Level;
  capacity: number;
  active?: boolean;
  isPaid?: boolean;
  price?: number;
  imageUrl?: string | null;
}

export interface CourseUpdateRequest {
  title: string;
  description: string;
  level: Level;
  capacity: number;
  active: boolean;
  isPaid: boolean;
  price: number;
  imageUrl?: string | null;
}

export interface CourseBulkCreateRequest {
  title: string;
  description: string;
  level: Level;
  capacity: number;
  active?: boolean;
  isPaid?: boolean;
  price?: number;
  imageUrl?: string | null;
  chapters: ChapterBulkRequest[];
}

export interface ChapterBulkRequest {
  title: string;
  orderIndex: number;
  lessons: LessonBulkRequest[];
}

export interface LessonBulkRequest {
  title: string;
  orderIndex: number;
  xpReward?: number;
}

export interface ChapterResponse {
  id: string;
  courseId: string;
  title: string;
  orderIndex: number;
  createdAt: string;
  updatedAt: string;
}

export interface ChapterCreateRequest {
  courseId: string;
  title: string;
  orderIndex: number;
}

export interface ChapterUpdateRequest {
  title: string;
  orderIndex: number;
}

export interface LessonResponse {
  id: string;
  chapterId: string;
  courseId: string;
  title: string;
  orderIndex: number;
  xpReward: number;
  createdAt: string;
  updatedAt: string;
}

export interface LessonCreateRequest {
  chapterId: string;
  title: string;
  orderIndex: number;
  xpReward: number;
}

export interface LessonUpdateRequest {
  title: string;
  orderIndex: number;
  xpReward: number;
}

export interface MaterialResponse {
  id: string;
  lessonId: string;
  type: MaterialType;
  title: string;
  url?: string;
  content?: string;
  fileName?: string;
  fileType?: string;
  hasFile?: boolean;
  createdAt: string;
}

export interface MaterialCreateRequest {
  lessonId: string;
  type: MaterialType;
  title: string;
  url?: string;
  content?: string;
}

export interface MaterialUpdateRequest {
  type: MaterialType;
  title: string;
  url?: string;
  content?: string;
}

export interface EnrollmentRequest {
  courseId: string;
}

export interface EnrollmentResponse {
  id: string;
  courseId: string;
  userId: string;
  status: EnrollmentStatus;
  progressPercent?: number;
  xpEarned?: number;
  lastMilestone?: number;
  completionBadge?: string;
  enrolledAt?: string;
  completedAt?: string;
}

export interface LessonCompletionRequest {
  lessonId: string;
}

export interface ProgressResponse {
  courseId: string;
  progressPercent?: number;
  completedLessons?: number;
  totalLessons?: number;
  milestoneReached?: number;
  nextLessonId?: string;
  completedLessonIds?: string[];
}

export interface PaymentCheckoutRequest {
  courseId: string;
}

export interface PaymentCheckoutResponse {
  paymentId: string;
  courseId: string;
  status: 'SUCCESS' | 'REQUIRES_PAYMENT';
  clientSecret: string;
  sessionUrl?: string;
  publicKey: string;
  enrollment: EnrollmentResponse;
}

export interface CourseReviewCreateRequest {
  rating: number;
  comment: string;
}

export interface CourseReviewResponse {
  id: string;
  courseId: string;
  userId: string;
  rating: number;
  comment: string;
  createdAt: string;
  updatedAt: string;
  mine: boolean;
}

export interface CourseReviewSummaryResponse {
  courseId: string;
  averageRating: number;
  totalReviews: number;
}
