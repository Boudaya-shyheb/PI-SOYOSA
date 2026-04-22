package com.englishway.course.service;

import com.englishway.course.dto.EnrollmentResponse;
import com.englishway.course.dto.EnrollmentRequest;
import com.englishway.course.dto.ProgressResponse;
import com.englishway.course.entity.Course;
import com.englishway.course.entity.Enrollment;
import com.englishway.course.entity.Lesson;
import com.englishway.course.entity.LessonCompletion;
import com.englishway.course.enums.EnrollmentStatus;
import com.englishway.course.event.CourseCompletedEvent;
import com.englishway.course.event.CourseProgressUpdatedEvent;
import com.englishway.course.event.EventPublisher;
import com.englishway.course.event.LessonCompletedEvent;
import com.englishway.course.event.PaymentConfirmedEvent;
import com.englishway.course.event.StudentEnrolledEvent;
import com.englishway.course.exception.BadRequestException;
import com.englishway.course.exception.NotFoundException;
import com.englishway.course.repository.CourseRepository;
import com.englishway.course.repository.EnrollmentRepository;
import com.englishway.course.repository.LearningMaterialRepository;
import com.englishway.course.repository.LessonCompletionRepository;
import com.englishway.course.repository.LessonRepository;
import com.englishway.course.util.RequestContext;
import com.englishway.course.client.UserServiceClient;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonCompletionRepository completionRepository;
    private final LearningMaterialRepository materialRepository;
    private final EventPublisher eventPublisher;
    private final AccessControlService accessControlService;
    private final UserServiceFeignService userServiceFeignService;

    public EnrollmentService(
        CourseRepository courseRepository,
        EnrollmentRepository enrollmentRepository,
        LessonRepository lessonRepository,
        LessonCompletionRepository completionRepository,
        LearningMaterialRepository materialRepository,
        EventPublisher eventPublisher,
        AccessControlService accessControlService,
        UserServiceFeignService userServiceFeignService
    ) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonRepository = lessonRepository;
        this.completionRepository = completionRepository;
        this.materialRepository = materialRepository;
        this.eventPublisher = eventPublisher;
        this.accessControlService = accessControlService;
        this.userServiceFeignService = userServiceFeignService;
    }

    @Transactional
    public EnrollmentResponse requestEnrollment(RequestContext context, UUID courseId) {
        accessControlService.requireStudent(context);
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found"));
        if (!course.isActive()) {
            throw new BadRequestException("Course is not active");
        }
        if (course.getCapacity() != null && course.getCapacity() < 0) {
            throw new BadRequestException("Course capacity cannot be negative");
        }
        Enrollment existing = enrollmentRepository.findByCourseIdAndUserId(courseId, context.getUserId())
            .orElse(null);
        if (existing != null) {
            if (existing.getStatus() == EnrollmentStatus.PENDING && !course.isPaid()) {
                existing.setStatus(EnrollmentStatus.ACTIVE);
                enrollmentRepository.save(existing);
            } else if (existing.getStatus() == EnrollmentStatus.COMPLETED) {
                completionRepository.deleteByEnrollmentId(existing.getId());
                existing.setStatus(EnrollmentStatus.ACTIVE);
                existing.setProgressPercent(0);
                existing.setXpEarned(0);
                existing.setLastMilestone(null);
                existing.setCompletedAt(null);
                existing.setCompletionBadge(null);
                enrollmentRepository.save(existing);
            }
            return toResponse(existing);
        }
        ensureCapacity(courseId, course.getCapacity());
        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setUserId(context.getUserId());
        enrollment.setStatus(course.isPaid() ? EnrollmentStatus.PENDING : EnrollmentStatus.ACTIVE);
        enrollment.setProgressPercent(0);
        enrollment.setXpEarned(0);
        Enrollment saved = enrollmentRepository.save(enrollment);
        if (saved.getStatus() == EnrollmentStatus.ACTIVE) {
            eventPublisher.publishStudentEnrolled(new StudentEnrolledEvent(courseId, context.getUserId(), saved.getStatus()));
        }
        return toResponse(saved);
    }

    @Transactional
    public void activateEnrollmentFromPayment(PaymentConfirmedEvent event) {
        activateEnrollmentAfterPayment(event.getUserId(), event.getCourseId());
    }

    @Transactional
    public EnrollmentResponse activateEnrollmentAfterPayment(String userId, UUID courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found"));
        if (!course.isActive()) {
            throw new BadRequestException("Course is not active");
        }
        if (!course.isPaid()) {
            throw new BadRequestException("Course does not require payment");
        }
        if (course.getCapacity() != null && course.getCapacity() < 0) {
            throw new BadRequestException("Course capacity cannot be negative");
        }
        Enrollment enrollment = enrollmentRepository.findByCourseIdAndUserId(courseId, userId)
            .orElse(null);
        if (enrollment == null) {
            ensureCapacity(course.getId(), course.getCapacity());
            enrollment = new Enrollment();
            enrollment.setCourse(course);
            enrollment.setUserId(userId);
            enrollment.setProgressPercent(0);
            enrollment.setXpEarned(0);
        }
        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
        }
        Enrollment saved = enrollmentRepository.save(enrollment);
        eventPublisher.publishStudentEnrolled(
            new StudentEnrolledEvent(course.getId(), userId, saved.getStatus())
        );
        return toResponse(saved);
    }

    public EnrollmentResponse getEnrollment(UUID courseId, String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        return enrollmentRepository.findByCourseIdAndUserId(courseId, userId)
            .map(this::toResponse)
            .orElse(null);
    }

    public EnrollmentResponse getEnrollmentById(RequestContext context, UUID enrollmentId) {
        accessControlService.requireAuthenticated(context);
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new NotFoundException("Enrollment not found"));
        
        // Ensure student can only see their own enrollment
        if (context.getRole() == com.englishway.course.enums.Role.STUDENT && 
            !enrollment.getUserId().equals(context.getUserId())) {
            throw new com.englishway.course.exception.AccessDeniedException("You do not have permission to view this enrollment");
        }
        
        return toResponse(enrollment);
    }

    @Transactional
    public ProgressResponse completeLesson(RequestContext context, UUID courseId, UUID lessonId) {
        accessControlService.requireStudent(context);
        Enrollment enrollment = enrollmentRepository.findByCourseIdAndUserId(courseId, context.getUserId())
            .orElseThrow(() -> new NotFoundException("Enrollment not found"));
        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new NotFoundException("Lesson not found"));
        if (!lesson.getChapter().getCourse().getId().equals(courseId)) {
            throw new BadRequestException("Lesson does not belong to course");
        }
        
        List<Lesson> orderedLessons = lessonRepository.findByCourseIdOrdered(courseId);
        int index = indexOfLesson(orderedLessons, lessonId);
        if (index == -1) {
            throw new BadRequestException("Lesson order not found");
        }

        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            // Allow PENDING enrollments to complete the first lesson (Free Preview)
            if (!(enrollment.getStatus() == EnrollmentStatus.PENDING && index == 0)) {
                throw new BadRequestException("Enrollment is not active");
            }
        }        if (completionRepository.findByEnrollmentIdAndLessonId(enrollment.getId(), lessonId).isPresent()) {
            throw new BadRequestException("Lesson already completed");
        }
        if (index > 0) {
            List<UUID> previousIds = orderedLessons.subList(0, index).stream()
                .map(Lesson::getId)
                .collect(Collectors.toList());
            long completedCount = completionRepository.countByEnrollmentIdAndLessonIdIn(enrollment.getId(), previousIds);
            if (completedCount != previousIds.size()) {
                throw new BadRequestException("Lesson is locked until previous lessons are completed");
            }
        }
        LessonCompletion completion = new LessonCompletion();
        completion.setEnrollment(enrollment);
        completion.setLesson(lesson);
        completion.setXpReward(lesson.getXpReward());
        completionRepository.save(completion);

        int xpEarned = enrollment.getXpEarned() + lesson.getXpReward();
        int completedLessons = (int) completionRepository.countByEnrollmentId(enrollment.getId());
        int totalLessons = orderedLessons.size();
        int progressPercent = totalLessons == 0 ? 0 : (completedLessons * 100) / totalLessons;
        int milestone = milestoneFor(progressPercent);

        enrollment.setXpEarned(xpEarned);
        enrollment.setProgressPercent(progressPercent);
        if (milestone > 0 && (enrollment.getLastMilestone() == null || milestone > enrollment.getLastMilestone())) {
            enrollment.setLastMilestone(milestone);
        }
        if (progressPercent >= 100) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(Instant.now());
            enrollment.setCompletionBadge("COURSE_COMPLETED");
        }
        enrollmentRepository.save(enrollment);

        eventPublisher.publishLessonCompleted(
            new LessonCompletedEvent(courseId, lessonId, context.getUserId(), lesson.getXpReward())
        );
        eventPublisher.publishCourseProgressUpdated(
            new CourseProgressUpdatedEvent(courseId, context.getUserId(), progressPercent, enrollment.getLastMilestone())
        );
        if (progressPercent >= 100) {
            eventPublisher.publishCourseCompleted(
                new CourseCompletedEvent(courseId, context.getUserId(), enrollment.getCompletionBadge())
            );
        }
        return buildProgressResponse(courseId, enrollment, orderedLessons, completedLessons);
    }

    public ProgressResponse getProgress(RequestContext context, UUID courseId) {
        accessControlService.requireStudent(context);
        Enrollment enrollment = enrollmentRepository.findByCourseIdAndUserId(courseId, context.getUserId())
            .orElseThrow(() -> new NotFoundException("Enrollment not found"));
        List<Lesson> orderedLessons = lessonRepository.findByCourseIdOrdered(courseId);
        int completedLessons = (int) completionRepository.countByEnrollmentId(enrollment.getId());
        return buildProgressResponse(courseId, enrollment, orderedLessons, completedLessons);
    }

    public UUID getNextLessonRecommendation(RequestContext context, UUID courseId) {
        accessControlService.requireStudent(context);
        Enrollment enrollment = enrollmentRepository.findByCourseIdAndUserId(courseId, context.getUserId())
            .orElseThrow(() -> new NotFoundException("Enrollment not found"));
        List<Lesson> orderedLessons = lessonRepository.findByCourseIdOrdered(courseId);
        return nextLessonId(orderedLessons, enrollment.getId());
    }

    private ProgressResponse buildProgressResponse(
        UUID courseId,
        Enrollment enrollment,
        List<Lesson> orderedLessons,
        int completedLessons
    ) {
        int totalLessons = orderedLessons.size();
        ProgressResponse response = new ProgressResponse();
        response.setCourseId(courseId);
        response.setProgressPercent(enrollment.getProgressPercent());
        response.setCompletedLessons(completedLessons);
        response.setTotalLessons(totalLessons);
        response.setMilestoneReached(enrollment.getLastMilestone());
        response.setNextLessonId(nextLessonId(orderedLessons, enrollment.getId()));
        response.setCompletedLessonIds(getCompletedLessonIds(enrollment.getId()));
        return response;
    }

    private List<UUID> getCompletedLessonIds(UUID enrollmentId) {
        return completionRepository.findByEnrollmentId(enrollmentId).stream()
            .map(completion -> completion.getLesson().getId())
            .collect(Collectors.toList());
    }

    private UUID nextLessonId(List<Lesson> orderedLessons, UUID enrollmentId) {
        List<UUID> completedLessonIds = completionRepository.findByEnrollmentId(enrollmentId).stream()
            .map(completion -> completion.getLesson().getId())
            .collect(Collectors.toList());
        for (Lesson lesson : orderedLessons) {
            if (!completedLessonIds.contains(lesson.getId())) {
                return lesson.getId();
            }
        }
        return null;
    }

    private void ensureCapacity(UUID courseId, Integer capacity) {
        // Capacity limits are intentionally disabled to allow unlimited enrollments.
    }

    private int milestoneFor(int progressPercent) {
        if (progressPercent >= 100) {
            return 100;
        }
        if (progressPercent >= 75) {
            return 75;
        }
        if (progressPercent >= 50) {
            return 50;
        }
        if (progressPercent >= 25) {
            return 25;
        }
        return 0;
    }

    private int indexOfLesson(List<Lesson> lessons, UUID lessonId) {
        for (int i = 0; i < lessons.size(); i++) {
            if (lessons.get(i).getId().equals(lessonId)) {
                return i;
            }
        }
        return -1;
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(enrollment.getId());
        response.setCourseId(enrollment.getCourse().getId());
        response.setUserId(enrollment.getUserId());
        
        // Fetch and set user name
        try {
            UserServiceClient.UserInfoDto userInfo = userServiceFeignService.getUserInfo(enrollment.getUserId());
            response.setUserName(userInfo.getDisplayName());
        } catch (Exception e) {
            response.setUserName(enrollment.getUserId());
        }
        
        response.setStatus(enrollment.getStatus());
        response.setProgressPercent(enrollment.getProgressPercent());
        response.setXpEarned(enrollment.getXpEarned());
        response.setLastMilestone(enrollment.getLastMilestone());
        response.setCompletionBadge(enrollment.getCompletionBadge());
        response.setEnrolledAt(enrollment.getEnrolledAt());
        response.setCompletedAt(enrollment.getCompletedAt());
        return response;
    }

    @Transactional
    public EnrollmentResponse updateEnrollment(RequestContext context, UUID enrollmentId, EnrollmentRequest request) {
        accessControlService.requireStudent(context);
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new NotFoundException("Enrollment not found"));
        
        // Students can only update their own enrollments
        if (!context.getUserId().equals(enrollment.getUserId())) {
            throw new BadRequestException("You can only update your own enrollments");
        }
        
        // Currently supports only re-activating completed courses
        if (enrollment.getStatus() == EnrollmentStatus.COMPLETED && request.getCourseId().equals(enrollment.getCourse().getId())) {
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            enrollment.setProgressPercent(0);
            enrollment.setXpEarned(0);
            enrollment.setLastMilestone(null);
            enrollment.setCompletedAt(null);
            enrollment.setCompletionBadge(null);
            completionRepository.deleteByEnrollmentId(enrollment.getId());
        }
        
        Enrollment saved = enrollmentRepository.save(enrollment);
        return toResponse(saved);
    }

    @Transactional
    public void cancelEnrollment(RequestContext context, UUID enrollmentId) {
        accessControlService.requireStudent(context);
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new NotFoundException("Enrollment not found"));
        
        // Students can only cancel their own enrollments
        if (!context.getUserId().equals(enrollment.getUserId())) {
            throw new BadRequestException("You can only cancel your own enrollments");
        }
        
        // Users can only cancel PENDING or ACTIVE enrollments
        if (enrollment.getStatus() == EnrollmentStatus.PENDING || enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
            completionRepository.deleteByEnrollmentId(enrollment.getId());
            enrollmentRepository.deleteById(enrollmentId);
        } else {
            throw new BadRequestException("Cannot cancel enrollment with status: " + enrollment.getStatus());
        }
    }
}
