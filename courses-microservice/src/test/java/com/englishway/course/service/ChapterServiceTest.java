package com.englishway.course.service;

import com.englishway.course.dto.ChapterCreateRequest;
import com.englishway.course.dto.ChapterResponse;
import com.englishway.course.dto.ChapterUpdateRequest;
import com.englishway.course.entity.Chapter;
import com.englishway.course.entity.Course;
import com.englishway.course.enums.Level;
import com.englishway.course.exception.NotFoundException;
import com.englishway.course.repository.ChapterRepository;
import com.englishway.course.repository.CourseRepository;
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
@DisplayName("ChapterService Tests")
class ChapterServiceTest {

    @Mock private ChapterRepository chapterRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private AccessControlService accessControlService;

    @InjectMocks
    private ChapterService chapterService;

    private UUID courseId;
    private UUID chapterId;
    private RequestContext tutorContext;
    private Course course;
    private Chapter chapter;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        chapterId = UUID.randomUUID();
        tutorContext = RequestContext.fromHeaders("tutor-1", "TUTOR");

        course = new Course();
        course.setId(courseId);
        course.setTitle("English Grammar");
        course.setLevel(Level.B1);
        course.setTutorId("tutor-1");

        chapter = new Chapter();
        chapter.setId(chapterId);
        chapter.setCourse(course);
        chapter.setTitle("Chapter 1: Basics");
        chapter.setOrderIndex(1);
    }

    @Test
    @DisplayName("createChapter: saves chapter and returns response")
    void createChapter_success() {
        ChapterCreateRequest request = new ChapterCreateRequest();
        request.setCourseId(courseId);
        request.setTitle("Chapter 1: Basics");
        request.setOrderIndex(1);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(chapterRepository.save(any(Chapter.class))).thenAnswer(inv -> {
            Chapter c = inv.getArgument(0);
            c.setId(chapterId);
            return c;
        });

        ChapterResponse response = chapterService.createChapter(tutorContext, request);

        assertThat(response.getTitle()).isEqualTo("Chapter 1: Basics");
        assertThat(response.getCourseId()).isEqualTo(courseId);
    }

    @Test
    @DisplayName("createChapter: throws NotFoundException for unknown course")
    void createChapter_courseNotFound() {
        ChapterCreateRequest request = new ChapterCreateRequest();
        request.setCourseId(courseId);
        request.setTitle("Any Chapter");

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chapterService.createChapter(tutorContext, request))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Course not found");
    }

    @Test
    @DisplayName("getChapter: returns chapter when it exists")
    void getChapter_found() {
        when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));

        ChapterResponse response = chapterService.getChapter(chapterId);

        assertThat(response.getId()).isEqualTo(chapterId);
        assertThat(response.getTitle()).isEqualTo("Chapter 1: Basics");
    }

    @Test
    @DisplayName("getChapter: throws NotFoundException for unknown chapter")
    void getChapter_notFound() {
        when(chapterRepository.findById(chapterId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chapterService.getChapter(chapterId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Chapter not found");
    }

    @Test
    @DisplayName("listChapters: returns ordered list")
    void listChapters_success() {
        when(chapterRepository.findByCourseIdOrderByOrderIndex(courseId)).thenReturn(List.of(chapter));

        List<ChapterResponse> result = chapterService.listChapters(courseId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Chapter 1: Basics");
    }

    @Test
    @DisplayName("updateChapter: updates title and order index")
    void updateChapter_success() {
        ChapterUpdateRequest request = new ChapterUpdateRequest();
        request.setTitle("Chapter 1: Updated Basics");
        request.setOrderIndex(2);

        when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));
        when(chapterRepository.save(any(Chapter.class))).thenAnswer(inv -> inv.getArgument(0));

        ChapterResponse response = chapterService.updateChapter(tutorContext, chapterId, request);

        assertThat(response.getTitle()).isEqualTo("Chapter 1: Updated Basics");
        assertThat(response.getOrderIndex()).isEqualTo(2);
    }

    @Test
    @DisplayName("deleteChapter: deletes successfully")
    void deleteChapter_success() {
        when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));

        chapterService.deleteChapter(tutorContext, chapterId);

        verify(chapterRepository).delete(chapter);
    }

    @Test
    @DisplayName("deleteChapter: throws NotFoundException for unknown chapter")
    void deleteChapter_notFound() {
        when(chapterRepository.findById(chapterId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chapterService.deleteChapter(tutorContext, chapterId))
            .isInstanceOf(NotFoundException.class);
    }
}
