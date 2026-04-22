export interface QuizAttempt {
  id: string;
  courseId: string;
  studentId?: string;
  lessonId?: string;
  quizId?: string;
  score: number | null;
  passingScore?: number | null;
  passed?: boolean | null;
  attemptedAt: string;
  answers?: any[];
  gradedAt?: string | null;
  gradedBy?: string | null;
}

export interface Certificate {
  certificateId: string;
  courseId: string;
  studentId: string;
  finalScore: number;
  completedAt: string;
}
