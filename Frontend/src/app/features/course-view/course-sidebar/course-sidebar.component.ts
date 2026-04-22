import { Component, Input } from '@angular/core';
import { CourseResponse } from '../../../models/api-models';
import { ChapterWithLessons } from '../course-view.component';

@Component({
  selector: 'app-course-sidebar',
  templateUrl: './course-sidebar.component.html',
  styleUrls: ['./course-sidebar.component.css']
})
export class CourseSidebarComponent {
  @Input() course: CourseResponse | null = null;
  @Input() courseId = '';
  @Input() chapters: ChapterWithLessons[] = [];
  @Input() allCompleted = false;
  @Input() completionPercent = 0;

  get totalLessons(): number {
    return this.chapters.reduce((total, chapter) => total + chapter.lessons.length, 0);
  }

  get completedLessons(): number {
    return this.chapters.reduce(
      (total, chapter) => total + chapter.lessons.filter(lesson => lesson.completed).length,
      0
    );
  }

  get ringOffset(): number {
    const radius = 42;
    const circumference = 2 * Math.PI * radius;
    return circumference - (circumference * this.completionPercent) / 100;
  }

  get upcomingDeadlines(): Array<{ title: string; dueDate: string; priority: 'High' | 'Medium' | 'Low' }> {
    const upcoming = this.chapters
      .flatMap(chapter => chapter.lessons)
      .filter(lesson => !lesson.completed)
      .slice(0, 3);

    return upcoming.map((lesson, index) => {
      const due = new Date();
      due.setDate(due.getDate() + (index + 1) * 2);

      return {
        title: lesson.title,
        dueDate: due.toLocaleDateString(undefined, { month: 'short', day: 'numeric' }),
        priority: index === 0 ? 'High' : index === 1 ? 'Medium' : 'Low'
      };
    });
  }

  get instructorName(): string {
    return this.course?.tutorName || 'Course Instructor';
  }

  get instructorBio(): string {
    return 'Academic mentor focused on practical outcomes, structured feedback, and clear weekly progression.';
  }

  get initialLetter(): string {
    return this.instructorName.charAt(0).toUpperCase();
  }

  get finalExamLessonId(): string | null {
    const allLessons = this.chapters.flatMap(chapter => chapter.lessons);
    const explicit = allLessons.find(lesson => {
      const normalized = lesson.title.toLowerCase();
      return normalized.includes('final') || normalized.includes('exam') || normalized.includes('quiz');
    });
    return explicit?.id ?? allLessons[allLessons.length - 1]?.id ?? null;
  }

  get canOpenFinalExam(): boolean {
    return !!this.finalExamLessonId && !!this.courseId && this.allCompleted;
  }

  get finalExamRoute(): string[] {
    const lessonId = this.finalExamLessonId;
    if (!lessonId || !this.courseId) {
      return ['/courses', this.courseId];
    }
    return ['/courses', this.courseId, 'lessons', lessonId, 'quiz'];
  }
}
