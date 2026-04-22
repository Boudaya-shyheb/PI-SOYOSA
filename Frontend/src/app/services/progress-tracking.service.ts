import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

export interface StudentProgress {
  courseId: string;
  currentLessonId: string | null;
  currentChapterId: string | null;
  completedLessonIds: string[];
  lastAccessedAt: string;
  progressPercent: number;
}

interface ProgressStorage {
  [courseId: string]: StudentProgress;
}

@Injectable({ providedIn: 'root' })
export class ProgressTrackingService {
  private readonly storageKey = 'student-course-progress';

  getProgress(courseId: string): Observable<StudentProgress | null> {
    const progress = this.readProgress(courseId);
    return of(progress);
  }

  getAllProgress(): Observable<StudentProgress[]> {
    const storage = this.readStorage();
    return of(Object.values(storage));
  }

  updateCurrentLesson(courseId: string, lessonId: string, chapterId: string): void {
    let progress = this.readProgress(courseId);
    
    if (!progress) {
      progress = {
        courseId,
        currentLessonId: lessonId,
        currentChapterId: chapterId,
        completedLessonIds: [],
        lastAccessedAt: new Date().toISOString(),
        progressPercent: 0
      };
    } else {
      progress.currentLessonId = lessonId;
      progress.currentChapterId = chapterId;
      progress.lastAccessedAt = new Date().toISOString();
    }

    this.saveProgress(progress);
  }

  markLessonComplete(courseId: string, lessonId: string, totalLessons: number): void {
    let progress = this.readProgress(courseId);
    
    if (!progress) {
      progress = {
        courseId,
        currentLessonId: null,
        currentChapterId: null,
        completedLessonIds: [lessonId],
        lastAccessedAt: new Date().toISOString(),
        progressPercent: 0
      };
    }

    // Add to completed if not already there
    if (!progress.completedLessonIds.includes(lessonId)) {
      progress.completedLessonIds.push(lessonId);
    }

    // Calculate progress percentage
    if (totalLessons > 0) {
      progress.progressPercent = Math.round((progress.completedLessonIds.length / totalLessons) * 100);
    }

    progress.lastAccessedAt = new Date().toISOString();
    this.saveProgress(progress);
  }

  isLessonCompleted(courseId: string, lessonId: string): boolean {
    const progress = this.readProgress(courseId);
    return progress ? progress.completedLessonIds.includes(lessonId) : false;
  }

  resetProgress(courseId: string): void {
    const storage = this.readStorage();
    delete storage[courseId];
    this.writeStorage(storage);
  }

  private readProgress(courseId: string): StudentProgress | null {
    const storage = this.readStorage();
    return storage[courseId] || null;
  }

  private saveProgress(progress: StudentProgress): void {
    const storage = this.readStorage();
    storage[progress.courseId] = progress;
    this.writeStorage(storage);
  }

  private readStorage(): ProgressStorage {
    const raw = sessionStorage.getItem(this.storageKey);
    if (!raw) return {};
    
    try {
      const parsed = JSON.parse(raw) as ProgressStorage;
      return parsed && typeof parsed === 'object' ? parsed : {};
    } catch {
      return {};
    }
  }

  private writeStorage(storage: ProgressStorage): void {
    sessionStorage.setItem(this.storageKey, JSON.stringify(storage));
  }
}
