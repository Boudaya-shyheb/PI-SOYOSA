import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { COURSES, EVENTS, CLUBS, INSTRUCTORS } from '../data/constants';
import { DEFAULT_COURSE_IMAGE, DEFAULT_COURSE_PRICE } from '../data/course-display.config';
import { CourseApiService } from './course-api.service';
import { CourseDisplayService } from './course-display.service';
import { CourseResponse, Level } from '../models/api-models';
import { Course, SchoolEvent, Club, Instructor } from '../models/models';
import { COURSE_API_BASE_URL } from './api.config';

@Injectable({ providedIn: 'root' })
export class DataService {
  constructor(private coursesApi: CourseApiService, private display: CourseDisplayService) {}

  getCourses(): Observable<Course[]> {
    return this.coursesApi.listCourses(0, 50).pipe(
      map(page => page.content ?? []),
      map(courses => courses.map((course, index) => this.mapApiCourse(course, index))),
      catchError(() => of(COURSES))
    );
  }

  getEvents(): Observable<SchoolEvent[]> { return of(EVENTS); }
  getClubs(): Observable<Club[]> { return of(CLUBS); }
  getInstructors(): Observable<Instructor[]> { return of(INSTRUCTORS); }

  private mapApiCourse(course: CourseResponse, index: number): Course {
    const override = this.display.getOverride(course.id);
    const price = override.price ?? DEFAULT_COURSE_PRICE;
    const image = this.resolveCourseImage(course, override.image, index);

    return {
      id: course.id,
      title: course.title,
      description: course.description,
      price,
      oldPrice: undefined,
      type: 'Self-paced',
      level: this.mapLevel(course.level),
      duration: 'Flexible',
      image,
      tags: []
    };
  }

  private resolveCourseImage(course: CourseResponse, overrideImage: string | undefined, index: number): string {
    const candidates = [course.imageUrl ?? undefined, overrideImage]
      .map(candidate => this.resolveImageUrl(candidate))
      .filter((candidate): candidate is string => !!candidate);
    const selected = candidates.find(candidate => this.isValidImageUrl(candidate));
    return selected ?? `${DEFAULT_COURSE_IMAGE}?index=${index}`;
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

  private mapLevel(level: Level): Course['level'] {
    if (level === 'A1' || level === 'A2') {
      return 'Beginner';
    }
    if (level === 'B1' || level === 'B2') {
      return 'Mid-level';
    }
    return 'Advanced';
  }
}
