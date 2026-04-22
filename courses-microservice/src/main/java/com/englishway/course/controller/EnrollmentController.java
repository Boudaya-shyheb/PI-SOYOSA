package com.englishway.course.controller;

import com.englishway.course.dto.EnrollmentRequest;
import com.englishway.course.dto.EnrollmentResponse;
import com.englishway.course.dto.LessonCompletionRequest;
import com.englishway.course.dto.ProgressResponse;
import com.englishway.course.service.EnrollmentService;
import com.englishway.course.util.RequestContext;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public EnrollmentResponse requestEnrollment(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody EnrollmentRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        return enrollmentService.requestEnrollment(context, request.getCourseId());
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<EnrollmentResponse> getEnrollment(
        @PathVariable("courseId") UUID courseId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        EnrollmentResponse response = enrollmentService.getEnrollment(courseId, context.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/id/{enrollmentId}")
    public ResponseEntity<EnrollmentResponse> getEnrollmentById(
        @PathVariable("enrollmentId") UUID enrollmentId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        EnrollmentResponse response = enrollmentService.getEnrollmentById(context, enrollmentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{courseId}/payment-success")
    public EnrollmentResponse handlePaymentSuccess(
        @PathVariable("courseId") UUID courseId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        // Ensure user is a student to prevent unauthorized actions
        if (!"STUDENT".equals(context.getRole())) {
            throw new com.englishway.course.exception.BadRequestException("Only students can activate enrollments via payment");
        }
        return enrollmentService.activateEnrollmentAfterPayment(context.getUserId(), courseId);
    }

    @PostMapping("/{courseId}/lessons/complete")
    public ProgressResponse completeLesson(
        @PathVariable("courseId") UUID courseId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody LessonCompletionRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        return enrollmentService.completeLesson(context, courseId, request.getLessonId());
    }

    @GetMapping("/{courseId}/progress")
    public ProgressResponse getProgress(
        @PathVariable("courseId") UUID courseId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        return enrollmentService.getProgress(context, courseId);
    }

    @GetMapping("/{courseId}/next-lesson")
    public UUID getNextLesson(
        @PathVariable("courseId") UUID courseId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        return enrollmentService.getNextLessonRecommendation(context, courseId);
    }

    @PutMapping("/{enrollmentId}")
    public EnrollmentResponse updateEnrollment(
        @PathVariable("enrollmentId") UUID enrollmentId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody EnrollmentRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        return enrollmentService.updateEnrollment(context, enrollmentId, request);
    }

    @DeleteMapping("/{enrollmentId}")
    public void cancelEnrollment(
        @PathVariable("enrollmentId") UUID enrollmentId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        enrollmentService.cancelEnrollment(context, enrollmentId);
    }
}
