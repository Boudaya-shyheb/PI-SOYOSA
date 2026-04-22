import { Injectable } from '@angular/core';
import { Quiz, QuizQuestion } from '../models/quiz-models';

export interface QuizValidationResult {
  valid: boolean;
  errors: string[];
}

@Injectable({ providedIn: 'root' })
export class QuizValidationService {
  
  /**
   * Validate a complete quiz
   */
  validateQuiz(quiz: Quiz): QuizValidationResult {
    const errors: string[] = [];

    // Check quiz title
    if (!quiz.title || !quiz.title.trim()) {
      errors.push('Quiz title is required.');
    }

    // Check time limit
    if (!quiz.timeLimitMin || quiz.timeLimitMin < 1) {
      errors.push('Time limit must be at least 1 minute.');
    }

    // Check passing score
    if (quiz.passingScore < 0 || quiz.passingScore > 100) {
      errors.push('Passing score must be between 0 and 100.');
    }

    // Check questions
    if (!quiz.questions || quiz.questions.length === 0) {
      errors.push('Quiz must have at least one question.');
    } else {
      quiz.questions.forEach((q, index) => {
        const questionErrors = this.validateQuestion(q, index + 1);
        errors.push(...questionErrors);
      });
    }

    return {
      valid: errors.length === 0,
      errors
    };
  }

  /**
   * Validate a single question
   */
  validateQuestion(question: QuizQuestion, questionNumber: number): string[] {
    const errors: string[] = [];

    if (!question.text || !question.text.trim()) {
      errors.push(`Question ${questionNumber}: Text is required.`);
    }

    if (!question.options || question.options.length < 2) {
      errors.push(`Question ${questionNumber}: Must have at least 2 options.`);
    } else {
      const validOptions = question.options.filter(opt => opt && opt.trim());
      if (validOptions.length < 2) {
        errors.push(`Question ${questionNumber}: Must have at least 2 non-empty options.`);
      }
    }

    if (question.correctIndex < 0 || question.correctIndex >= (question.options?.length || 0)) {
      errors.push(`Question ${questionNumber}: Correct option index is out of range.`);
    }

    return errors;
  }

  /**
   * Calculate quiz score from attempt
   */
  calculateScore(quiz: Quiz, answers: number[]): number {
    if (!quiz.questions || quiz.questions.length === 0) {
      return 0;
    }

    const correct = quiz.questions.reduce((count, question, index) => {
      return count + (answers[index] === question.correctIndex ? 1 : 0);
    }, 0);

    return Math.round((correct / quiz.questions.length) * 100);
  }

  /**
   * Check if score passes the quiz
   */
  isPassing(score: number, passingScore: number): boolean {
    return score >= passingScore;
  }
}
