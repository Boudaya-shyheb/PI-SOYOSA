package com.englishway.course.controller;

import com.englishway.course.dto.CourseBulkCreateRequest;
import com.englishway.course.dto.CourseCreateRequest;
import com.englishway.course.dto.CourseResponse;
import com.englishway.course.dto.CourseUpdateRequest;
import com.englishway.course.service.AccessControlService;
import com.englishway.course.service.CourseService;
import com.englishway.course.util.RequestContext;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseService courseService;
    private final AccessControlService accessControlService;

    public CourseController(CourseService courseService, AccessControlService accessControlService) {
        this.courseService = courseService;
        this.accessControlService = accessControlService;
    }

    @PostMapping
    public CourseResponse createCourse(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody CourseCreateRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        accessControlService.requireContentWrite(context.getRole());
        return courseService.createCourse(context, request);
    }

    @PostMapping("/bulk")
    public CourseResponse bulkCreateCourse(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody CourseBulkCreateRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        accessControlService.requireContentWrite(context.getRole());
        return courseService.bulkCreateCourse(context, request);
    }

    @PutMapping("/{courseId}")
    public CourseResponse updateCourse(
        @PathVariable("courseId") UUID courseId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody CourseUpdateRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        accessControlService.requireContentWrite(context.getRole());
        return courseService.updateCourse(context, courseId, request);
    }

    @PatchMapping("/{courseId}/activation")
    public CourseResponse setActivation(
        @PathVariable("courseId") UUID courseId,
        @RequestParam boolean active,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        accessControlService.requireContentWrite(context.getRole());
        return courseService.setActive(context, courseId, active);
    }

    @GetMapping("/{courseId}")
    public CourseResponse getCourse(@PathVariable("courseId") UUID courseId) {
        return courseService.getCourse(courseId);
    }

    @GetMapping
    public Page<CourseResponse> listCourses(Pageable pageable) {
        return courseService.listCourses(pageable);
    }

    @DeleteMapping("/{courseId}")
    public void deleteCourse(
        @PathVariable("courseId") UUID courseId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        accessControlService.requireContentWrite(context.getRole());
        courseService.deleteCourse(context, courseId);
    }
}
