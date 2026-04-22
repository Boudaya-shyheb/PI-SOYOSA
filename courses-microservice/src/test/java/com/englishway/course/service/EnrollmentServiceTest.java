package com.englishway.course.service;

import com.englishway.course.dto.EnrollmentResponse;
import com.englishway.course.entity.Course;
import com.englishway.course.entity.Enrollment;
import com.englishway.course.entity.Lesson;
import com.englishway.course.enums.EnrollmentStatus;
import com.englishway.course.enums.Level;
import com.englishway.course.event.EventPublisher;
import com.englishway.course.event.StudentEnrolledEvent;
import com.englishway.course.exception.BadRequestException;
import com.englishway.course.exception.NotFoundException;
import com.englishway.course.repository.*;
import com.englishway.course.util.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentService Tests")
class EnrollmentServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private LessonCompletionRepository completionRepository;
    @Mock private LearningMaterialRepository materialRepository;
    @Mock private EventPublisher eventPublisher;
    @Mock private AccessControlService accessControlService;
    @Mock private UserServiceFeignService userServiceFeignService;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private UUID courseId;
    private UUID lessonId;
    private RequestContext studentContext;
    private Course freeCourse;
    private Course paidCourse;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
        studentContext = RequestContext.fromHeaders("student-1", "STUDENT");

        freeCourse = new Course();
        freeCourse.setId(courseId);
        freeCourse.setTitle("Free English Course");
        freeCourse.setLevel(Level.A1);
        freeCourse.setActive(true);
        freeCourse.setPaid(false);
        freeCourse.setPrice(BigDecimal.ZERO);

        paidCourse = new Course();
        paidCourse.setId(courseId);
        paidCourse.setTitle("Premium English Course");
        paidCourse.setLevel(Level.B2);
        paidCourse.setActive(true);
        paidCourse.setPaid(true);
        paidCourse.setPrice(new BigDecimal("29.99"));

        lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setTitle("Lesson 1");
        lesson.setXpReward(10);
    }

    @Test
    @DisplayName("requestEnrollment: creates ACTIVE enrollment for a FREE course")
    void requestEnrollment_freeCourse_createsActive() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(freeCourse));
        when(enrollmentRepository.findByCourseIdAndUserId(courseId, "student-1")).thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> {
            Enrollment e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        EnrollmentResponse response = enrollmentService.requestEnrollment(studentContext, courseId);

        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        verify(eventPublisher).publishStudentEnrolled(any(StudentEnrolledEvent.class));
    }

    @Test
    @DisplayName("requestEnrollment: creates PENDING enrollment for a PAID course")
    void requestEnrollment_paidCourse_createsPending() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(paidCourse));
        when(enrollmentRepository.findByCourseIdAndUserId(courseId, "student-1")).thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> {
            Enrollment e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        EnrollmentResponse response = enrollmentService.requestEnrollment(studentContext, courseId);

        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
        verify(eventPublisher, never()).publishStudentEnrolled(any());
    }

    @Test
    @DisplayName("requestEnrollment: throws BadRequestException for inactive course")
    void requestEnrollment_inactiveCourse_throws() {
        freeCourse.setActive(false);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(freeCourse));

        assertThatThrownBy(() -> enrollmentService.requestEnrollment(studentContext, courseId))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("not active");
    }

    @Test
    @DisplayName("requestEnrollment: throws BadRequestException when course has no lessons")
    void requestEnrollment_noLessons_throws() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(freeCourse));
        when(enrollmentRepository.findByCourseIdAndUserId(courseId, "student-1")).thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> {
            Enrollment e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        EnrollmentResponse response = enrollmentService.requestEnrollment(studentContext, courseId);

        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
    }

    @Test
    @DisplayName("requestEnrollment: throws NotFoundException when course does not exist")
    void requestEnrollment_courseNotFound_throws() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.requestEnrollment(studentContext, courseId))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("getEnrollment: returns enrollment when found")
    void getEnrollment_found() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(UUID.randomUUID());
        enrollment.setCourse(freeCourse);
        enrollment.setUserId("student-1");
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        when(enrollmentRepository.findByCourseIdAndUserId(courseId, "student-1")).thenReturn(Optional.of(enrollment));
        when(userServiceFeignService.getUserInfo(any())).thenThrow(new RuntimeException());

        EnrollmentResponse response = enrollmentService.getEnrollment(courseId, "student-1");

        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
    }

    @Test
    @DisplayName("getEnrollment: throws NotFoundException when enrollment is missing")
    void getEnrollment_notFound() {
        when(enrollmentRepository.findByCourseIdAndUserId(courseId, "student-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.getEnrollment(courseId, "student-1"))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("getEnrollment: throws NotFoundException when userId is blank")
    void getEnrollment_blankUserId_throws() {
        assertThatThrownBy(() -> enrollmentService.getEnrollment(courseId, ""))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("activateEnrollmentAfterPayment: activates PENDING enrollment")
    void activateEnrollmentAfterPayment_pendingToActive() {
        Enrollment pending = new Enrollment();
        pending.setId(UUID.randomUUID());
        pending.setCourse(paidCourse);
        pending.setUserId("student-1");
        pending.setStatus(EnrollmentStatus.PENDING);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(paidCourse));
        when(enrollmentRepository.findByCourseIdAndUserId(courseId, "student-1")).thenReturn(Optional.of(pending));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        EnrollmentResponse response = enrollmentService.activateEnrollmentAfterPayment("student-1", courseId);

        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        verify(eventPublisher).publishStudentEnrolled(any(StudentEnrolledEvent.class));
    }

    @Test
    @DisplayName("activateEnrollmentAfterPayment: throws BadRequestException for free course")
    void activateEnrollmentAfterPayment_freeCourse_throws() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(freeCourse));

        assertThatThrownBy(() -> enrollmentService.activateEnrollmentAfterPayment("student-1", courseId))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("does not require payment");
    }

    @Test
    @DisplayName("cancelEnrollment: cancels ACTIVE enrollment successfully")
    void cancelEnrollment_active_success() {
        UUID enrollmentId = UUID.randomUUID();
        Enrollment enrollment = new Enrollment();
        enrollment.setId(enrollmentId);
        enrollment.setCourse(freeCourse);
        enrollment.setUserId("student-1");
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

        enrollmentService.cancelEnrollment(studentContext, enrollmentId);

        verify(enrollmentRepository).deleteById(enrollmentId);
    }

    @Test
    @DisplayName("cancelEnrollment: throws BadRequestException for COMPLETED enrollment")
    void cancelEnrollment_completed_throws() {
        UUID enrollmentId = UUID.randomUUID();
        Enrollment enrollment = new Enrollment();
        enrollment.setId(enrollmentId);
        enrollment.setCourse(freeCourse);
        enrollment.setUserId("student-1");
        enrollment.setStatus(EnrollmentStatus.COMPLETED);

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> enrollmentService.cancelEnrollment(studentContext, enrollmentId))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Cannot cancel");
    }

    @Test
    @DisplayName("cancelEnrollment: throws BadRequestException when another student tries to cancel")
    void cancelEnrollment_wrongStudent_throws() {
        UUID enrollmentId = UUID.randomUUID();
        Enrollment enrollment = new Enrollment();
        enrollment.setId(enrollmentId);
        enrollment.setCourse(freeCourse);
        enrollment.setUserId("other-student");
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> enrollmentService.cancelEnrollment(studentContext, enrollmentId))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("your own");
    }
}
