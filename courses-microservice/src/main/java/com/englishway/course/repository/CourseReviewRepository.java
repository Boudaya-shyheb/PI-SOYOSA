package com.englishway.course.repository;

import com.englishway.course.entity.CourseReview;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseReviewRepository extends JpaRepository<CourseReview, UUID> {
    List<CourseReview> findByCourseIdOrderByUpdatedAtDesc(UUID courseId);

    Optional<CourseReview> findByCourseIdAndUserId(UUID courseId, String userId);

    @Query("select coalesce(avg(r.rating), 0) from CourseReview r where r.course.id = :courseId")
    Double averageRating(@Param("courseId") UUID courseId);

    long countByCourseId(UUID courseId);
}
