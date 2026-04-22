import { SimpleChange } from '@angular/core';
import { CourseContentComponent } from './course-content.component';
import { ChapterWithLessons } from '../course-view.component';

describe('CourseContentComponent', () => {
  let component: CourseContentComponent;

  const chaptersFixture: ChapterWithLessons[] = [
    {
      id: 'ch-1',
      courseId: 'course-1',
      title: 'Introduction',
      orderIndex: 1,
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
      lessons: [
        {
          id: 'l-1',
          chapterId: 'ch-1',
          courseId: 'course-1',
          title: 'Welcome video',
          orderIndex: 1,
          xpReward: 10,
          createdAt: '2026-01-01T00:00:00Z',
          updatedAt: '2026-01-01T00:00:00Z',
          completed: true,
          duration: '10mins'
        },
        {
          id: 'l-2',
          chapterId: 'ch-1',
          courseId: 'course-1',
          title: 'Reading basics',
          orderIndex: 2,
          xpReward: 10,
          createdAt: '2026-01-01T00:00:00Z',
          updatedAt: '2026-01-01T00:00:00Z',
          completed: false,
          duration: '15mins'
        }
      ],
      expanded: false
    },
    {
      id: 'ch-2',
      courseId: 'course-1',
      title: 'Final assessment',
      orderIndex: 2,
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
      lessons: [
        {
          id: 'l-3',
          chapterId: 'ch-2',
          courseId: 'course-1',
          title: 'Final quiz',
          orderIndex: 1,
          xpReward: 20,
          createdAt: '2026-01-01T00:00:00Z',
          updatedAt: '2026-01-01T00:00:00Z',
          completed: false,
          duration: '20mins'
        }
      ],
      expanded: false
    }
  ];

  beforeEach(() => {
    component = new CourseContentComponent();
    component.courseId = 'course-1';
    component.chapters = chaptersFixture.map(chapter => ({
      ...chapter,
      lessons: chapter.lessons.map(lesson => ({ ...lesson }))
    }));
  });

  it('selects first chapter with incomplete lesson on changes', () => {
    component.ngOnChanges({
      chapters: new SimpleChange([], component.chapters, true)
    });

    expect(component.activeModuleId).toBe('ch-1');
    expect(component.isExpanded('ch-1')).toBeTrue();
  });

  it('toggles chapter expansion state', () => {
    component.toggleModule('ch-1');
    expect(component.isExpanded('ch-1')).toBeTrue();

    component.toggleModule('ch-1');
    expect(component.isExpanded('ch-1')).toBeFalse();
  });

  it('filters chapters by lesson title while preserving chapter context', () => {
    component.searchQuery = 'reading';

    const result = component.filteredChapters;

    expect(result.length).toBe(1);
    expect(result[0].id).toBe('ch-1');
    expect(result[0].lessons.length).toBe(1);
    expect(result[0].lessons[0].id).toBe('l-2');
  });

  it('builds resume route from first incomplete lesson', () => {
    expect(component.firstIncompleteLessonId).toBe('l-2');
    expect(component.resumeRoute).toEqual(['/lessons', 'l-2']);
  });

  it('builds final exam route from detected final lesson', () => {
    expect(component.finalExamLessonId).toBe('l-3');
    expect(component.finalExamRoute).toEqual(['/courses', 'course-1', 'lessons', 'l-3', 'quiz']);
  });

  it('calculates module metadata from lesson durations', () => {
    const chapter = component.chapters[0];

    expect(component.moduleMeta(chapter)).toBe('2 lessons • 25 mins');
  });

  it('maps lesson kind and icon consistently', () => {
    expect(component.lessonKind('Final Quiz')).toBe('quiz');
    expect(component.lessonIcon('quiz')).toBe('Q');
    expect(component.lessonKind('Homework Project')).toBe('assignment');
    expect(component.lessonIcon('assignment')).toBe('A');
  });

  it('emits lesson toggled and certificate download events', () => {
    const lessonSpy = jasmine.createSpy('lessonSpy');
    const downloadSpy = jasmine.createSpy('downloadSpy');
    component.lessonToggled.subscribe(lessonSpy);
    component.downloadCertificate.subscribe(downloadSpy);

    component.markLessonComplete('l-2', true);
    component.onDownloadCertificate();

    expect(lessonSpy).toHaveBeenCalledWith({ lessonId: 'l-2', completed: true });
    expect(downloadSpy).toHaveBeenCalled();
  });
});