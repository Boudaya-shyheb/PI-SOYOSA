package com.englishway.course.service;

import com.englishway.course.dto.ChapterCreateRequest;
import com.englishway.course.dto.ChapterResponse;
import com.englishway.course.dto.ChapterUpdateRequest;
import com.englishway.course.entity.Chapter;
import com.englishway.course.entity.Course;
import com.englishway.course.exception.NotFoundException;
import com.englishway.course.repository.ChapterRepository;
import com.englishway.course.repository.CourseRepository;
import com.englishway.course.util.RequestContext;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChapterService {
    private final ChapterRepository chapterRepository;
    private final CourseRepository courseRepository;
    private final AccessControlService accessControlService;

    public ChapterService(
        ChapterRepository chapterRepository,
        CourseRepository courseRepository,
        AccessControlService accessControlService
    ) {
        this.chapterRepository = chapterRepository;
        this.courseRepository = courseRepository;
        this.accessControlService = accessControlService;
    }

    @Transactional
    public ChapterResponse createChapter(RequestContext context, ChapterCreateRequest request) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());
        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new NotFoundException("Course not found"));
        accessControlService.requireCourseOwnership(context, course.getTutorId());
        Chapter chapter = new Chapter();
        chapter.setCourse(course);
        chapter.setTitle(request.getTitle());
        chapter.setOrderIndex(request.getOrderIndex());
        Chapter saved = chapterRepository.save(chapter);
        return toResponse(saved);
    }

    @Transactional
    public ChapterResponse updateChapter(RequestContext context, UUID chapterId, ChapterUpdateRequest request) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());
        Chapter chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> new NotFoundException("Chapter not found"));
        accessControlService.requireCourseOwnership(context, chapter.getCourse().getTutorId());
        chapter.setTitle(request.getTitle());
        chapter.setOrderIndex(request.getOrderIndex());
        Chapter saved = chapterRepository.save(chapter);
        return toResponse(saved);
    }

    @Transactional
    public void deleteChapter(RequestContext context, UUID chapterId) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());
        Chapter chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> new NotFoundException("Chapter not found"));
        accessControlService.requireCourseOwnership(context, chapter.getCourse().getTutorId());
        chapterRepository.delete(chapter);
    }

    public List<ChapterResponse> listChapters(UUID courseId) {
        return chapterRepository.findByCourseIdOrderByOrderIndex(courseId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public ChapterResponse getChapter(UUID chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> new NotFoundException("Chapter not found"));
        return toResponse(chapter);
    }

    private ChapterResponse toResponse(Chapter chapter) {
        ChapterResponse response = new ChapterResponse();
        response.setId(chapter.getId());
        response.setCourseId(chapter.getCourse().getId());
        response.setTitle(chapter.getTitle());
        response.setOrderIndex(chapter.getOrderIndex());
        response.setCreatedAt(chapter.getCreatedAt());
        response.setUpdatedAt(chapter.getUpdatedAt());
        return response;
    }
}
