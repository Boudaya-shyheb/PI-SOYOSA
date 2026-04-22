package com.englishway.course.service;

import com.englishway.course.dto.LessonCreateRequest;
import com.englishway.course.dto.LessonResponse;
import com.englishway.course.dto.LessonUpdateRequest;
import com.englishway.course.entity.Chapter;
import com.englishway.course.entity.Course;
import com.englishway.course.entity.Lesson;
import com.englishway.course.enums.Level;
import com.englishway.course.exception.NotFoundException;
import com.englishway.course.repository.ChapterRepository;
import com.englishway.course.repository.LessonRepository;
import com.englishway.course.util.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LessonService Tests")
class LessonServiceTest {

    @Mock private LessonRepository lessonRepository;
    @Mock private ChapterRepository chapterRepository;
    @Mock private AccessControlService accessControlService;

    @InjectMocks
    private LessonService lessonService;

    private UUID courseId;
    private UUID chapterId;
    private UUID lessonId;
    private RequestContext tutorContext;
    private Course course;
    private Chapter chapter;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        chapterId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
        tutorContext = RequestContext.fromHeaders("tutor-1", "TUTOR");

        course = new Course();
        course.setId(courseId);
        course.setLevel(Level.A2);
        course.setTutorId("tutor-1");

        chapter = new Chapter();
        chapter.setId(chapterId);
        chapter.setCourse(course);
        chapter.setTitle("Chapter 1");

        lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setChapter(chapter);
        lesson.setTitle("Lesson 1: Greetings");
        lesson.setOrderIndex(1);
        lesson.setXpReward(20);
    }

    @Test
    @DisplayName("createLesson: saves lesson successfully")
    void createLesson_success() {
        LessonCreateRequest request = new LessonCreateRequest();
        request.setChapterId(chapterId);
        request.setTitle("Lesson 1: Greetings");
        request.setOrderIndex(1);
        request.setXpReward(20);

        when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> {
            Lesson l = inv.getArgument(0);
            l.setId(lessonId);
            return l;
        });

        LessonResponse response = lessonService.createLesson(tutorContext, request);

        assertThat(response.getTitle()).isEqualTo("Lesson 1: Greetings");
        assertThat(response.getXpReward()).isEqualTo(20);
        assertThat(response.getCourseId()).isEqualTo(courseId);
    }

    @Test
    @DisplayName("createLesson: throws NotFoundException for unknown chapter")
    void createLesson_chapterNotFound() {
        LessonCreateRequest request = new LessonCreateRequest();
        request.setChapterId(chapterId);
        request.setTitle("Any Lesson");

        when(chapterRepository.findById(chapterId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.createLesson(tutorContext, request))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Chapter not found");
    }

    @Test
    @DisplayName("getLesson: returns lesson when found")
    void getLesson_found() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        LessonResponse response = lessonService.getLesson(lessonId);

        assertThat(response.getId()).isEqualTo(lessonId);
        assertThat(response.getTitle()).isEqualTo("Lesson 1: Greetings");
    }

    @Test
    @DisplayName("getLesson: throws NotFoundException when not found")
    void getLesson_notFound() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.getLesson(lessonId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Lesson not found");
    }

    @Test
    @DisplayName("updateLesson: updates fields correctly")
    void updateLesson_success() {
        LessonUpdateRequest request = new LessonUpdateRequest();
        request.setTitle("Lesson 1: Updated Greetings");
        request.setOrderIndex(2);
        request.setXpReward(30);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));

        LessonResponse response = lessonService.updateLesson(tutorContext, lessonId, request);

        assertThat(response.getTitle()).isEqualTo("Lesson 1: Updated Greetings");
        assertThat(response.getXpReward()).isEqualTo(30);
        assertThat(response.getOrderIndex()).isEqualTo(2);
    }

    @Test
    @DisplayName("deleteLesson: deletes successfully")
    void deleteLesson_success() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        lessonService.deleteLesson(tutorContext, lessonId);

        verify(lessonRepository).delete(lesson);
    }

    @Test
    @DisplayName("deleteLesson: throws NotFoundException for unknown lesson")
    void deleteLesson_notFound() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.deleteLesson(tutorContext, lessonId))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("listLessonsByChapter: returns lessons ordered")
    void listLessonsByChapter_success() {
        when(lessonRepository.findByChapterIdOrderByOrderIndex(chapterId)).thenReturn(List.of(lesson));

        List<LessonResponse> result = lessonService.listLessonsByChapter(chapterId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Lesson 1: Greetings");
    }
}
