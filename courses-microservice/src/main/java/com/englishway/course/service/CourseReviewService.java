package com.englishway.course.service;

import com.englishway.course.dto.CourseReviewCreateRequest;
import com.englishway.course.dto.CourseReviewResponse;
import com.englishway.course.dto.CourseReviewSummaryResponse;
import com.englishway.course.entity.Course;
import com.englishway.course.entity.CourseReview;
import com.englishway.course.entity.Enrollment;
import com.englishway.course.enums.EnrollmentStatus;
import com.englishway.course.enums.Role;
import com.englishway.course.exception.AccessDeniedException;
import com.englishway.course.exception.NotFoundException;
import com.englishway.course.repository.CourseRepository;
import com.englishway.course.repository.CourseReviewRepository;
import com.englishway.course.repository.EnrollmentRepository;
import com.englishway.course.util.RequestContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseReviewService {
    private final CourseRepository courseRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final EnrollmentRepository enrollmentRepository;

    public CourseReviewService(
        CourseRepository courseRepository,
        CourseReviewRepository courseReviewRepository,
        EnrollmentRepository enrollmentRepository
    ) {
        this.courseRepository = courseRepository;
        this.courseReviewRepository = courseReviewRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public List<CourseReviewResponse> listReviews(UUID courseId, RequestContext context) {
        ensureCourseExists(courseId);
        String viewerId = context != null ? context.getUserId() : null;
        return courseReviewRepository.findByCourseIdOrderByUpdatedAtDesc(courseId)
            .stream()
            .map(review -> toResponse(review, viewerId))
            .toList();
    }

    public CourseReviewSummaryResponse getSummary(UUID courseId) {
        ensureCourseExists(courseId);
        Double averageRaw = courseReviewRepository.averageRating(courseId);
        long total = courseReviewRepository.countByCourseId(courseId);

        CourseReviewSummaryResponse response = new CourseReviewSummaryResponse();
        response.setCourseId(courseId);
        response.setAverageRating(round(averageRaw != null ? averageRaw : 0));
        response.setTotalReviews(total);
        return response;
    }

    @Transactional
    public CourseReviewResponse upsertReview(UUID courseId, CourseReviewCreateRequest request, RequestContext context) {
        if (!context.isAuthenticated() || context.getRole() != Role.STUDENT) {
            throw new AccessDeniedException("Only authenticated students can submit course reviews");
        }

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found"));

        Enrollment enrollment = enrollmentRepository.findByCourseIdAndUserId(courseId, context.getUserId())
            .orElseThrow(() -> new AccessDeniedException("You must be enrolled in this course to review it"));

        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE && enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
            throw new AccessDeniedException("Only active or completed enrollments can submit reviews");
        }

        CourseReview review = courseReviewRepository.findByCourseIdAndUserId(courseId, context.getUserId())
            .orElseGet(() -> {
                CourseReview created = new CourseReview();
                created.setCourse(course);
                created.setUserId(context.getUserId());
                return created;
            });

        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());

        CourseReview saved = courseReviewRepository.save(review);
        return toResponse(saved, context.getUserId());
    }

    private void ensureCourseExists(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new NotFoundException("Course not found");
        }
    }

    private CourseReviewResponse toResponse(CourseReview review, String viewerId) {
        CourseReviewResponse response = new CourseReviewResponse();
        response.setId(review.getId());
        response.setCourseId(review.getCourse().getId());
        response.setUserId(review.getUserId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        response.setMine(viewerId != null && viewerId.equals(review.getUserId()));
        return response;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
