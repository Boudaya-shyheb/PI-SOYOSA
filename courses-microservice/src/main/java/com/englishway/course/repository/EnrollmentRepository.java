package com.englishway.course.repository;

import com.englishway.course.entity.Enrollment;
import com.englishway.course.enums.EnrollmentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    Optional<Enrollment> findByCourseIdAndUserId(UUID courseId, String userId);

    long countByCourseIdAndStatusIn(UUID courseId, List<EnrollmentStatus> statuses);
}
