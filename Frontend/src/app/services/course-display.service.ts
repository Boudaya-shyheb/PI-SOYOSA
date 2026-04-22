import { Injectable } from '@angular/core';
import { COURSE_DISPLAY_OVERRIDES, CourseDisplayOverride } from '../data/course-display.config';

interface CourseDisplayStorage {
  [courseId: string]: CourseDisplayOverride;
}

@Injectable({ providedIn: 'root' })
export class CourseDisplayService {
  private readonly storageKey = 'jungle-course-display';

  getOverride(courseId: string): CourseDisplayOverride {
    const stored = this.readStorage();
    return { ...COURSE_DISPLAY_OVERRIDES[courseId], ...stored[courseId] };
  }

  setOverride(courseId: string, override: CourseDisplayOverride): void {
    const stored = this.readStorage();
    stored[courseId] = { ...stored[courseId], ...override };
    this.writeStorage(stored);
  }

  removeOverride(courseId: string): void {
    const stored = this.readStorage();
    delete stored[courseId];
    this.writeStorage(stored);
  }

  private readStorage(): CourseDisplayStorage {
    const raw = sessionStorage.getItem(this.storageKey);
    if (!raw) {
      return {};
    }
    try {
      const parsed = JSON.parse(raw) as CourseDisplayStorage;
      return parsed && typeof parsed === 'object' ? parsed : {};
    } catch {
      return {};
    }
  }

  private writeStorage(data: CourseDisplayStorage): void {
    sessionStorage.setItem(this.storageKey, JSON.stringify(data));
  }
}
