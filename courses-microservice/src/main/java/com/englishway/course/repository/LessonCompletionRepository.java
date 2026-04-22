package com.englishway.course.repository;

import com.englishway.course.entity.LessonCompletion;
import java.util.List;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, UUID> {
    Optional<LessonCompletion> findByEnrollmentIdAndLessonId(UUID enrollmentId, UUID lessonId);

    List<LessonCompletion> findByEnrollmentId(UUID enrollmentId);

    void deleteByEnrollmentId(UUID enrollmentId);

    long countByEnrollmentId(UUID enrollmentId);

    long countByEnrollmentIdAndLessonIdIn(UUID enrollmentId, Collection<UUID> lessonIds);
}
