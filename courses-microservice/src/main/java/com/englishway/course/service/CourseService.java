package com.englishway.course.service;

import com.englishway.course.dto.CourseBulkCreateRequest;
import com.englishway.course.dto.CourseCreateRequest;
import com.englishway.course.dto.CourseResponse;
import com.englishway.course.dto.CourseUpdateRequest;
import com.englishway.course.entity.Chapter;
import com.englishway.course.entity.Course;
import com.englishway.course.entity.Lesson;
import com.englishway.course.event.CourseCreatedEvent;
import com.englishway.course.event.CourseUpdatedEvent;
import com.englishway.course.event.EventPublisher;
import com.englishway.course.exception.NotFoundException;
import com.englishway.course.repository.ChapterRepository;
import com.englishway.course.repository.CourseRepository;
import com.englishway.course.repository.LessonRepository;
import com.englishway.course.util.RequestContext;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final AccessControlService accessControlService;
    private final EventPublisher eventPublisher;
    private final CloudinaryStorageService cloudinaryStorageService;

    public CourseService(
        CourseRepository courseRepository,
        ChapterRepository chapterRepository,
        LessonRepository lessonRepository,
        AccessControlService accessControlService,
        EventPublisher eventPublisher,
        CloudinaryStorageService cloudinaryStorageService
    ) {
        this.courseRepository = courseRepository;
        this.chapterRepository = chapterRepository;
        this.lessonRepository = lessonRepository;
        this.accessControlService = accessControlService;
        this.eventPublisher = eventPublisher;
        this.cloudinaryStorageService = cloudinaryStorageService;
    }

    @Transactional
    public CourseResponse bulkCreateCourse(RequestContext context, CourseBulkCreateRequest request) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());
        
        // 1. Create Course
        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setLevel(request.getLevel());
        course.setCapacity(request.getCapacity());
        course.setActive(request.getActive() != null ? request.getActive() : true);
        boolean isPaid = request.getIsPaid() != null ? request.getIsPaid() : false;
        course.setPaid(isPaid);
        course.setPrice(normalizePrice(request.getPrice(), isPaid));
        course.setTutorId(context.getUserId());
        course.setImageUrl(resolveImageUrl(request.getImageUrl(), context.getUserId()));
        Course savedCourse = courseRepository.save(course);
        courseRepository.flush();

        // 2. Create Chapters and Lessons
        if (request.getChapters() != null) {
            for (CourseBulkCreateRequest.ChapterBulkRequest chapterReq : request.getChapters()) {
                Chapter chapter = new Chapter();
                chapter.setCourse(savedCourse);
                chapter.setTitle(chapterReq.getTitle());
                chapter.setOrderIndex(chapterReq.getOrderIndex());
                Chapter savedChapter = chapterRepository.save(chapter);
                
                if (chapterReq.getLessons() != null) {
                    for (CourseBulkCreateRequest.LessonBulkRequest lessonReq : chapterReq.getLessons()) {
                        Lesson lesson = new Lesson();
                        lesson.setChapter(savedChapter);
                        lesson.setTitle(lessonReq.getTitle());
                        lesson.setOrderIndex(lessonReq.getOrderIndex());
                        lesson.setXpReward(lessonReq.getXpReward() != null ? lessonReq.getXpReward() : 10);
                        lessonRepository.save(lesson);
                    }
                }
            }
        }

        eventPublisher.publishCourseCreated(
            new CourseCreatedEvent(savedCourse.getId(), savedCourse.getTitle(), savedCourse.getLevel(), savedCourse.isActive())
        );
        
        return toResponse(savedCourse);
    }

    @Transactional
    public CourseResponse createCourse(RequestContext context, CourseCreateRequest request) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());
        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setLevel(request.getLevel());
        course.setCapacity(request.getCapacity());
        course.setActive(request.getActive() != null ? request.getActive() : true);
        boolean isPaid = request.getIsPaid() != null ? request.getIsPaid() : false;
        course.setPaid(isPaid);
        course.setPrice(normalizePrice(request.getPrice(), isPaid));
        course.setTutorId(context.getUserId());
        course.setImageUrl(resolveImageUrl(request.getImageUrl(), context.getUserId()));
        Course saved = courseRepository.save(course);
        courseRepository.flush();
        eventPublisher.publishCourseCreated(
            new CourseCreatedEvent(saved.getId(), saved.getTitle(), saved.getLevel(), saved.isActive())
        );
        return toResponse(saved);
    }

    @Transactional
    public CourseResponse updateCourse(RequestContext context, UUID courseId, CourseUpdateRequest request) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found"));
        accessControlService.requireCourseOwnership(context, course.getTutorId());
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setLevel(request.getLevel());
        course.setCapacity(request.getCapacity());
        course.setActive(request.getActive());
        course.setPaid(request.getIsPaid());
        course.setPrice(normalizePrice(request.getPrice(), request.getIsPaid()));
        if (request.getImageUrl() != null) {
            course.setImageUrl(resolveImageUrl(request.getImageUrl(), context.getUserId()));
        }
        Course saved = courseRepository.save(course);
        eventPublisher.publishCourseUpdated(new CourseUpdatedEvent(saved.getId()));
        return toResponse(saved);
    }

    @Transactional
    public void deleteCourse(RequestContext context, UUID courseId) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found"));
        accessControlService.requireCourseOwnership(context, course.getTutorId());
        courseRepository.delete(course);
    }

    public CourseResponse getCourse(UUID courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found"));
        return toResponse(course);
    }

    public Page<CourseResponse> listCourses(Pageable pageable) {
        return courseRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public CourseResponse setActive(RequestContext context, UUID courseId, boolean active) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found"));
        accessControlService.requireCourseOwnership(context, course.getTutorId());
        course.setActive(active);
        Course saved = courseRepository.save(course);
        eventPublisher.publishCourseUpdated(new CourseUpdatedEvent(saved.getId()));
        return toResponse(saved);
    }

    private CourseResponse toResponse(Course course) {
        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setDescription(course.getDescription());
        response.setLevel(course.getLevel());
        response.setCapacity(course.getCapacity());
        response.setActive(course.isActive());
        response.setPaid(course.isPaid());
        response.setPrice(course.getPrice());
        response.setTutorId(course.getTutorId());
        response.setImageUrl(course.getImageUrl());
        response.setCreatedAt(course.getCreatedAt());
        response.setUpdatedAt(course.getUpdatedAt());
        response.setTotalChapters(chapterRepository.countByCourseId(course.getId()));
        response.setTotalLessons(lessonRepository.countByChapterCourseId(course.getId()));
        return response;
    }

    private String normalizeImageUrl(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String resolveImageUrl(String value, String userId) {
        String normalized = normalizeImageUrl(value);
        if (normalized == null) {
            return null;
        }

        if (normalized.startsWith("data:image/") && cloudinaryStorageService.isEnabled()) {
            String folder = "courses";
            if (userId != null && !userId.isBlank()) {
                folder = folder + "/" + userId;
            }
            return cloudinaryStorageService.uploadCourseImageDataUrl(normalized, folder);
        }

        return normalized;
    }

    private BigDecimal normalizePrice(BigDecimal input, boolean isPaid) {
        if (!isPaid) {
            return BigDecimal.ZERO;
        }
        if (input == null) {
            return BigDecimal.ZERO;
        }
        if (input.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return input.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
