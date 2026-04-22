import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CourseResponse } from '../../../models/api-models';
import { ChapterWithLessons } from '../course-view.component';

type LessonKind = 'video' | 'quiz' | 'reading' | 'assignment';

@Component({
  selector: 'app-course-content',
  templateUrl: './course-content.component.html',
  styleUrls: ['./course-content.component.css']
})
export class CourseContentComponent implements OnChanges {
  @Input() course: CourseResponse | null = null;
  @Input() courseId = '';
  @Input() chapters: ChapterWithLessons[] = [];
  @Input() allLessonsCompleted = false;
  @Input() certificateEligible = false;
  @Input() finalQuizPassed = false;

  @Output() downloadCertificate = new EventEmitter<void>();
  @Output() lessonToggled = new EventEmitter<{ lessonId: string; completed: boolean }>();

  tabs = [
    { id: 'about', label: 'About' },
    { id: 'content', label: 'Content' },
    { id: 'annexes', label: 'Annexes' },
    { id: 'forum', label: 'Forum' }
  ];
  activeTab = 'about';
  expandedDescription = false;

  searchQuery = '';
  activeModuleId = '';
  expandedModules = new Set<string>();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['chapters'] && this.chapters.length > 0) {
      const preferred = this.chapters.find(chapter => chapter.lessons.some(lesson => !lesson.completed));
      const nextActiveId = preferred?.id ?? this.chapters[0].id;

      this.activeModuleId = nextActiveId;
      if (!this.expandedModules.has(nextActiveId)) {
        this.expandedModules.add(nextActiveId);
      }
    }
  }

  toggleModule(chapterId: string): void {
    this.activeModuleId = chapterId;

    if (this.expandedModules.has(chapterId)) {
      this.expandedModules.delete(chapterId);
      return;
    }

    this.expandedModules.add(chapterId);
  }

  setActiveTab(tabId: string): void {
    this.activeTab = tabId;
  }

  isExpanded(chapterId: string): boolean {
    return this.expandedModules.has(chapterId);
  }

  markLessonComplete(lessonId: string, completed: boolean): void {
    this.lessonToggled.emit({ lessonId, completed });
  }

  onDownloadCertificate(): void {
    this.downloadCertificate.emit();
  }

  toggleDescription(): void {
    this.expandedDescription = !this.expandedDescription;
  }

  get courseDescription(): string {
    return this.course?.description || '';
  }

  get truncatedDescription(): string {
    const text = this.courseDescription;
    if (!text) {
      return '';
    }
    return text.length > 220 ? `${text.slice(0, 220)}...` : text;
  }

  get showReadMore(): boolean {
    return this.courseDescription.length > 220;
  }

  get currentChapterTitle(): string {
    const active = this.chapters.find(chapter => chapter.id === this.activeModuleId) || this.chapters[0];
    return active?.title || this.course?.title || 'Course Overview';
  }

  get contentSections(): Array<{ title: string; description: string }> {
    return this.filteredChapters.map(chapter => ({
      title: chapter.title,
      description: `${chapter.lessons.length} lessons`
    }));
  }

  get filteredChapters(): ChapterWithLessons[] {
    const query = this.searchQuery.trim().toLowerCase();
    if (!query) {
      return this.chapters;
    }

    return this.chapters
      .map(chapter => {
        const chapterMatches = chapter.title.toLowerCase().includes(query);
        const lessons = chapter.lessons.filter(lesson => lesson.title.toLowerCase().includes(query));

        if (chapterMatches) {
          return chapter;
        }

        return {
          ...chapter,
          lessons
        };
      })
      .filter(chapter => chapter.lessons.length > 0 || chapter.title.toLowerCase().includes(query));
  }

  lessonKind(title: string): LessonKind {
    const normalized = title.toLowerCase();
    if (normalized.includes('quiz') || normalized.includes('test') || normalized.includes('exam')) {
      return 'quiz';
    }
    if (normalized.includes('assignment') || normalized.includes('project') || normalized.includes('homework')) {
      return 'assignment';
    }
    if (normalized.includes('reading') || normalized.includes('article') || normalized.includes('text')) {
      return 'reading';
    }
    return 'video';
  }

  lessonIcon(kind: LessonKind): string {
    switch (kind) {
      case 'quiz':
        return 'Q';
      case 'reading':
        return 'R';
      case 'assignment':
        return 'A';
      default:
        return 'V';
    }
  }

  moduleMeta(chapter: ChapterWithLessons): string {
    const lessons = chapter.lessons.length;
    const minutes = chapter.lessons.reduce((total, lesson) => {
      const parsed = parseInt((lesson.duration || '').replace(/[^0-9]/g, ''), 10);
      return total + (Number.isNaN(parsed) ? 0 : parsed);
    }, 0);

    return `${lessons} lessons • ${minutes || 0} mins`;
  }

  get welcomeDescription(): string {
    return this.course?.description || 'Welcome to your course dashboard. Continue your weekly rhythm, complete the highlighted module, and keep your momentum toward certification.';
  }

  get firstIncompleteLessonId(): string | null {
    for (const chapter of this.chapters) {
      const lesson = chapter.lessons.find(item => !item.completed);
      if (lesson) {
        return lesson.id;
      }
    }
    return this.chapters[0]?.lessons[0]?.id ?? null;
  }

  get resumeRoute(): string[] {
    const lessonId = this.firstIncompleteLessonId;
    return lessonId ? ['/lessons', lessonId] : ['/courses', this.courseId];
  }

  get finalExamLessonId(): string | null {
    const allLessons = this.chapters.flatMap(chapter => chapter.lessons);
    const explicit = allLessons.find(lesson => {
      const normalized = lesson.title.toLowerCase();
      return normalized.includes('final') || normalized.includes('exam') || normalized.includes('quiz');
    });
    return explicit?.id ?? allLessons[allLessons.length - 1]?.id ?? null;
  }

  get finalExamRoute(): string[] {
    const lessonId = this.finalExamLessonId;
    if (!lessonId || !this.courseId) {
      return ['/courses', this.courseId];
    }
    return ['/courses', this.courseId, 'lessons', lessonId, 'quiz'];
  }
}
