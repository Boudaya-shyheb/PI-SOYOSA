package com.englishway.course.service;

import com.englishway.course.dto.LessonCreateRequest;
import com.englishway.course.dto.LessonResponse;
import com.englishway.course.dto.LessonUpdateRequest;
import com.englishway.course.entity.Chapter;
import com.englishway.course.entity.Lesson;
import com.englishway.course.exception.NotFoundException;
import com.englishway.course.repository.ChapterRepository;
import com.englishway.course.repository.LessonRepository;
import com.englishway.course.util.RequestContext;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LessonService {
    private final LessonRepository lessonRepository;
    private final ChapterRepository chapterRepository;
    private final AccessControlService accessControlService;

    public LessonService(
        LessonRepository lessonRepository,
        ChapterRepository chapterRepository,
        AccessControlService accessControlService
    ) {
        this.lessonRepository = lessonRepository;
        this.chapterRepository = chapterRepository;
        this.accessControlService = accessControlService;
    }

    @Transactional
    public LessonResponse createLesson(RequestContext context, LessonCreateRequest request) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());
        Chapter chapter = chapterRepository.findById(request.getChapterId())
            .orElseThrow(() -> new NotFoundException("Chapter not found"));
        accessControlService.requireCourseOwnership(context, chapter.getCourse().getTutorId());
        Lesson lesson = new Lesson();
        lesson.setChapter(chapter);
        lesson.setTitle(request.getTitle());
        lesson.setOrderIndex(request.getOrderIndex());
        lesson.setXpReward(request.getXpReward());
        Lesson saved = lessonRepository.save(lesson);
        return toResponse(saved);
    }

    @Transactional
    public LessonResponse updateLesson(RequestContext context, UUID lessonId, LessonUpdateRequest request) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());
        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new NotFoundException("Lesson not found"));
        accessControlService.requireCourseOwnership(context, lesson.getChapter().getCourse().getTutorId());
        lesson.setTitle(request.getTitle());
        lesson.setOrderIndex(request.getOrderIndex());
        lesson.setXpReward(request.getXpReward());
        Lesson saved = lessonRepository.save(lesson);
        return toResponse(saved);
    }

    @Transactional
    public void deleteLesson(RequestContext context, UUID lessonId) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());
        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new NotFoundException("Lesson not found"));
        accessControlService.requireCourseOwnership(context, lesson.getChapter().getCourse().getTutorId());
        lessonRepository.delete(lesson);
    }

    public List<LessonResponse> listLessonsByChapter(UUID chapterId) {
        return lessonRepository.findByChapterIdOrderByOrderIndex(chapterId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<LessonResponse> listLessonsByCourse(UUID courseId) {
        return lessonRepository.findByCourseIdOrdered(courseId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public LessonResponse getLesson(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new NotFoundException("Lesson not found"));
        return toResponse(lesson);
    }

    private LessonResponse toResponse(Lesson lesson) {
        LessonResponse response = new LessonResponse();
        response.setId(lesson.getId());
        response.setChapterId(lesson.getChapter().getId());
        response.setCourseId(lesson.getChapter().getCourse().getId());
        response.setTitle(lesson.getTitle());
        response.setOrderIndex(lesson.getOrderIndex());
        response.setXpReward(lesson.getXpReward());
        response.setCreatedAt(lesson.getCreatedAt());
        response.setUpdatedAt(lesson.getUpdatedAt());
        return response;
    }
}
