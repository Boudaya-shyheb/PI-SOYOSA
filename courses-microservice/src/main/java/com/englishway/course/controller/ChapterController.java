package com.englishway.course.controller;

import com.englishway.course.dto.ChapterCreateRequest;
import com.englishway.course.dto.ChapterResponse;
import com.englishway.course.dto.ChapterUpdateRequest;
import com.englishway.course.service.AccessControlService;
import com.englishway.course.service.ChapterService;
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
public class ChapterController {
    private final ChapterService chapterService;
    private final AccessControlService accessControlService;

    public ChapterController(ChapterService chapterService, AccessControlService accessControlService) {
        this.chapterService = chapterService;
        this.accessControlService = accessControlService;
    }

    @PostMapping("/chapters")
    public ChapterResponse createChapter(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody ChapterCreateRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        accessControlService.requireContentWrite(context.getRole());
        return chapterService.createChapter(context, request);
    }

    @PutMapping("/chapters/{chapterId}")
    public ChapterResponse updateChapter(
        @PathVariable("chapterId") UUID chapterId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody ChapterUpdateRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        accessControlService.requireContentWrite(context.getRole());
        return chapterService.updateChapter(context, chapterId, request);
    }

    @GetMapping("/chapters/{chapterId}")
    public ChapterResponse getChapter(@PathVariable("chapterId") UUID chapterId) {
        return chapterService.getChapter(chapterId);
    }

    @GetMapping("/courses/{courseId}/chapters")
    public List<ChapterResponse> listChapters(@PathVariable("courseId") UUID courseId) {
        return chapterService.listChapters(courseId);
    }

    @DeleteMapping("/chapters/{chapterId}")
    public void deleteChapter(
        @PathVariable("chapterId") UUID chapterId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        accessControlService.requireContentWrite(context.getRole());
        chapterService.deleteChapter(context, chapterId);
    }
}
