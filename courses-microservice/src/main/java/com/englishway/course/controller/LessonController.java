package com.englishway.course.controller;

import com.englishway.course.dto.LessonCreateRequest;
import com.englishway.course.dto.LessonResponse;
import com.englishway.course.dto.LessonUpdateRequest;
import com.englishway.course.service.AccessControlService;
import com.englishway.course.service.LessonService;
import com.englishway.course.util.RequestContext;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
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
@RequestMapping("/api")
public class LessonController {
    private final LessonService lessonService;
    private final AccessControlService accessControlService;

    public LessonController(LessonService lessonService, AccessControlService accessControlService) {
        this.lessonService = lessonService;
        this.accessControlService = accessControlService;
    }

    @PostMapping("/lessons")
    public LessonResponse createLesson(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody LessonCreateRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        accessControlService.requireContentWrite(context.getRole());
        return lessonService.createLesson(context, request);
    }

    @PutMapping("/lessons/{lessonId}")
    public LessonResponse updateLesson(
        @PathVariable("lessonId") UUID lessonId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody LessonUpdateRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        accessControlService.requireContentWrite(context.getRole());
        return lessonService.updateLesson(context, lessonId, request);
    }

    @GetMapping("/lessons/{lessonId}")
    public LessonResponse getLesson(@PathVariable("lessonId") UUID lessonId) {
        return lessonService.getLesson(lessonId);
    }

    @GetMapping("/chapters/{chapterId}/lessons")
    public List<LessonResponse> listLessonsByChapter(@PathVariable("chapterId") UUID chapterId) {
        return lessonService.listLessonsByChapter(chapterId);
    }

    @GetMapping("/courses/{courseId}/lessons")
    public List<LessonResponse> listLessonsByCourse(@PathVariable("courseId") UUID courseId) {
        return lessonService.listLessonsByCourse(courseId);
    }

    @DeleteMapping("/lessons/{lessonId}")
    public void deleteLesson(
        @PathVariable("lessonId") UUID lessonId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        accessControlService.requireContentWrite(context.getRole());
        lessonService.deleteLesson(context, lessonId);
    }
}
