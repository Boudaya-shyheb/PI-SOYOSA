package com.englishway.course.service;

import com.englishway.course.dto.CourseCreateRequest;
import com.englishway.course.dto.CourseResponse;
import com.englishway.course.dto.CourseUpdateRequest;
import com.englishway.course.entity.Course;
import com.englishway.course.enums.Level;
import com.englishway.course.event.CourseCreatedEvent;
import com.englishway.course.event.CourseUpdatedEvent;
import com.englishway.course.event.EventPublisher;
import com.englishway.course.exception.NotFoundException;
import com.englishway.course.repository.ChapterRepository;
import com.englishway.course.repository.CourseRepository;
import com.englishway.course.repository.LessonRepository;
import com.englishway.course.util.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService Tests")
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private ChapterRepository chapterRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private AccessControlService accessControlService;
    @Mock private EventPublisher eventPublisher;

    @InjectMocks
    private CourseService courseService;

    private RequestContext tutorContext;
    private UUID courseId;
    private Course course;

    @BeforeEach
    void setUp() {
        tutorContext = RequestContext.fromHeaders("tutor-1", "TUTOR");
        courseId = UUID.randomUUID();

        course = new Course();
        course.setId(courseId);
        course.setTitle("English for Beginners");
        course.setDescription("Learn English from scratch");
        course.setLevel(Level.A1);
        course.setCapacity(100);
        course.setActive(true);
        course.setPaid(false);
        course.setPrice(BigDecimal.ZERO);
        course.setTutorId("tutor-1");
    }

    @Test
    @DisplayName("createCourse: creates a free course successfully")
    void createCourse_free_success() {
        CourseCreateRequest request = new CourseCreateRequest();
        request.setTitle("English for Beginners");
        request.setDescription("Learn English from scratch");
        request.setLevel(Level.A1);
        request.setCapacity(100);
        request.setActive(true);
        request.setIsPaid(false);
        request.setPrice(null);

        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            c.setId(courseId);
            return c;
        });

        CourseResponse response = courseService.createCourse(tutorContext, request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("English for Beginners");
        assertThat(response.isPaid()).isFalse();
        assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(eventPublisher).publishCourseCreated(any(CourseCreatedEvent.class));
    }

    @Test
    @DisplayName("createCourse: creates a paid course and normalises price to 2dp")
    void createCourse_paid_normalisesPrice() {
        CourseCreateRequest request = new CourseCreateRequest();
        request.setTitle("Advanced Grammar");
        request.setDescription("Grammar deep dive");
        request.setLevel(Level.C1);
        request.setCapacity(50);
        request.setActive(true);
        request.setIsPaid(true);
        request.setPrice(new BigDecimal("29.999"));

        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            c.setId(courseId);
            return c;
        });

        CourseResponse response = courseService.createCourse(tutorContext, request);

        assertThat(response.isPaid()).isTrue();
        assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    @DisplayName("createCourse: sets price to ZERO when paid=false even if price provided")
    void createCourse_notPaid_priceIsZero() {
        CourseCreateRequest request = new CourseCreateRequest();
        request.setTitle("Free Course");
        request.setDescription("Free content");
        request.setLevel(Level.B1);
        request.setCapacity(50);
        request.setIsPaid(false);
        request.setPrice(new BigDecimal("99.00"));

        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            c.setId(courseId);
            return c;
        });

        CourseResponse response = courseService.createCourse(tutorContext, request);

        assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getCourse: returns course when it exists")
    void getCourse_found() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        CourseResponse response = courseService.getCourse(courseId);

        assertThat(response.getId()).isEqualTo(courseId);
        assertThat(response.getTitle()).isEqualTo("English for Beginners");
    }

    @Test
    @DisplayName("getCourse: throws NotFoundException when course does not exist")
    void getCourse_notFound() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourse(courseId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Course not found");
    }

    @Test
    @DisplayName("updateCourse: updates title and description")
    void updateCourse_success() {
        CourseUpdateRequest request = new CourseUpdateRequest();
        request.setTitle("English for Intermediate");
        request.setDescription("Updated description");
        request.setLevel(Level.B2);
        request.setCapacity(80);
        request.setActive(true);
        request.setIsPaid(false);
        request.setPrice(null);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseResponse response = courseService.updateCourse(tutorContext, courseId, request);

        assertThat(response.getTitle()).isEqualTo("English for Intermediate");
        verify(eventPublisher).publishCourseUpdated(any(CourseUpdatedEvent.class));
    }

    @Test
    @DisplayName("updateCourse: throws NotFoundException for unknown course")
    void updateCourse_notFound() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        CourseUpdateRequest request = new CourseUpdateRequest();
        request.setTitle("Test");
        request.setLevel(Level.A1);
        request.setActive(true);
        request.setIsPaid(false);

        assertThatThrownBy(() -> courseService.updateCourse(tutorContext, courseId, request))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("deleteCourse: deletes the course successfully")
    void deleteCourse_success() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        courseService.deleteCourse(tutorContext, courseId);

        verify(courseRepository).delete(course);
    }

    @Test
    @DisplayName("deleteCourse: throws NotFoundException for unknown course")
    void deleteCourse_notFound() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.deleteCourse(tutorContext, courseId))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("setActive: deactivates a course")
    void setActive_deactivate() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseResponse response = courseService.setActive(tutorContext, courseId, false);

        assertThat(response.isActive()).isFalse();
        verify(eventPublisher).publishCourseUpdated(any(CourseUpdatedEvent.class));
    }
}
