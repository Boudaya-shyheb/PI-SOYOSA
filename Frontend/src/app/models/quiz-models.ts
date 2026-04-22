export interface QuizQuestion {
  id: string;
  text: string;
  type: QuestionType;
  options: string[];
  correctIndex: number;
  explanation?: string;
  mediaUrl?: string;
  timeLimitSec?: number;
  points?: number;
  correctAnswer?: string;
}

export interface BaseQuiz {
  id: string;
  courseId: string;
  lessonId: string;
  title: string;
  timeLimitMin: number;
  passingScore: number;
  maxAttempts: number;
  cooldownMin: number;
  questions: QuizQuestion[];
  createdAt: string;
  updatedAt: string;
}

export type QuestionType = 'MULTIPLE_CHOICE' | 'TRUE_FALSE' | 'FILL_BLANK' | 'MEDIA';

export interface Quiz extends BaseQuiz {
  // This is the main Quiz model used throughout the app.
  // It can be extended for specific use cases.
}
