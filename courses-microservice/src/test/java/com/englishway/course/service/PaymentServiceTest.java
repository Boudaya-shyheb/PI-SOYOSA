package com.englishway.course.service;

import com.englishway.course.entity.Course;
import com.englishway.course.entity.Enrollment;
import com.englishway.course.enums.Level;
import com.englishway.course.exception.BadRequestException;
import com.englishway.course.exception.NotFoundException;
import com.englishway.course.repository.CourseRepository;
import com.englishway.course.repository.EnrollmentRepository;
import com.englishway.course.repository.LearningMaterialRepository;
import com.englishway.course.util.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private LearningMaterialRepository materialRepository;
    @Mock private EnrollmentService enrollmentService;
    @Mock private AccessControlService accessControlService;
    @Mock private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private UUID courseId;
    private RequestContext studentContext;
    private Course activePaidCourse;
    private Course freeCourse;
    private Course inactivePaidCourse;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        studentContext = RequestContext.fromHeaders("student-1", "STUDENT");

        ReflectionTestUtils.setField(paymentService, "stripePrivateKey", "sk_test_dummy");
        ReflectionTestUtils.setField(paymentService, "stripePublicKey", "pk_test_dummy");

        activePaidCourse = new Course();
        activePaidCourse.setId(courseId);
        activePaidCourse.setTitle("Premium Grammar");
        activePaidCourse.setLevel(Level.C1);
        activePaidCourse.setActive(true);
        activePaidCourse.setPaid(true);
        activePaidCourse.setPrice(new BigDecimal("29.99"));

        freeCourse = new Course();
        freeCourse.setId(courseId);
        freeCourse.setTitle("Free Course");
        freeCourse.setLevel(Level.A1);
        freeCourse.setActive(true);
        freeCourse.setPaid(false);
        freeCourse.setPrice(BigDecimal.ZERO);

        inactivePaidCourse = new Course();
        inactivePaidCourse.setId(courseId);
        inactivePaidCourse.setTitle("Inactive Premium Course");
        inactivePaidCourse.setLevel(Level.B1);
        inactivePaidCourse.setActive(false);
        inactivePaidCourse.setPaid(true);
        inactivePaidCourse.setPrice(new BigDecimal("19.99"));
    }

    @Test
    @DisplayName("checkoutCourse: throws NotFoundException when course does not exist")
    void checkoutCourse_courseNotFound_throws() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.checkoutCourse(studentContext, courseId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Course not found");
    }

    @Test
    @DisplayName("checkoutCourse: throws BadRequestException for inactive course")
    void checkoutCourse_inactiveCourse_throws() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(inactivePaidCourse));
        when(enrollmentRepository.findByCourseIdAndUserId(courseId, "student-1")).thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> paymentService.checkoutCourse(studentContext, courseId))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("not active");
    }

    @Test
    @DisplayName("checkoutCourse: throws BadRequestException for free course")
    void checkoutCourse_freeCourse_throws() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(freeCourse));
        when(enrollmentRepository.findByCourseIdAndUserId(courseId, "student-1")).thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> paymentService.checkoutCourse(studentContext, courseId))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("free");
    }

    @Test
    @DisplayName("checkoutCourse: surfaces Stripe errors for paid active courses")
    void checkoutCourse_stripeError_throws() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(activePaidCourse));
        when(enrollmentRepository.findByCourseIdAndUserId(courseId, "student-1")).thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> paymentService.checkoutCourse(studentContext, courseId))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Stripe error");
    }

    @Test
    @DisplayName("getPublicKey: returns the configured public key")
    void getPublicKey_returnsKey() {
        String key = paymentService.getPublicKey();
        assertThat(key).isEqualTo("pk_test_dummy");
    }
}
