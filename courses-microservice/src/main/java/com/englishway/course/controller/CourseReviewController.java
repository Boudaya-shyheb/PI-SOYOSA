package com.englishway.course.controller;

import com.englishway.course.dto.CourseReviewCreateRequest;
import com.englishway.course.dto.CourseReviewResponse;
import com.englishway.course.dto.CourseReviewSummaryResponse;
import com.englishway.course.service.CourseReviewService;
import com.englishway.course.util.RequestContext;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses/{courseId}/reviews")
public class CourseReviewController {
    private final CourseReviewService courseReviewService;

    public CourseReviewController(CourseReviewService courseReviewService) {
        this.courseReviewService = courseReviewService;
    }

    @GetMapping
    public List<CourseReviewResponse> listReviews(
        @PathVariable("courseId") UUID courseId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        return courseReviewService.listReviews(courseId, context);
    }

    @GetMapping("/summary")
    public CourseReviewSummaryResponse getSummary(@PathVariable("courseId") UUID courseId) {
        return courseReviewService.getSummary(courseId);
    }

    @PostMapping
    public CourseReviewResponse upsertReview(
        @PathVariable("courseId") UUID courseId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody CourseReviewCreateRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        return courseReviewService.upsertReview(courseId, request, context);
    }
}
