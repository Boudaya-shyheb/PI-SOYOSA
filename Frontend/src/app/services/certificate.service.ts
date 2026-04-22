import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ProgressResponse } from '../models/api-models';
import { QuizAttempt } from '../models/assessment-models';
import { COURSE_API_BASE_URL } from './api.config';
import { ApiHeadersService } from './api-headers.service';

@Injectable({ providedIn: 'root' })
export class CertificateService {
  /**
   * Check if student can download certificate
   * Updated logic: Student needs EITHER:
   * - 100% course completion AND passing quiz score, OR  
   * - Just a passing quiz score (quiz is sufficient alone)
   */
  canDownloadCertificate(progress: ProgressResponse | null, attempts: QuizAttempt[], passingScore: number): boolean {
    const progressComplete = (progress?.progressPercent ?? 0) >= 100;
    const passedQuiz = attempts.some(attempt => attempt.score !== null && attempt.score >= passingScore);
    
    // Certificate available if: 
    // 1. Course 100% complete AND quiz passed, OR
    // 2. Quiz passed (quiz alone is sufficient)
    return passedQuiz || (progressComplete && attempts.length > 0);
  }

  constructor(private http: HttpClient, private headers: ApiHeadersService) {}

  downloadCertificate(courseId: string): void {
    this.http.get(`${COURSE_API_BASE_URL}/certificates/${courseId}`, {
      headers: this.headers.buildHeaders(),
      responseType: 'blob'
    }).subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `certificate-${courseId}.pdf`;
      link.click();
      URL.revokeObjectURL(url);
    });
  }
}
