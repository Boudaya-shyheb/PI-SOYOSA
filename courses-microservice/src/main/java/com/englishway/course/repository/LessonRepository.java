package com.englishway.course.repository;

import com.englishway.course.entity.Lesson;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    List<Lesson> findByChapterIdOrderByOrderIndex(UUID chapterId);

    @Query("select l from Lesson l join l.chapter c where c.course.id = :courseId order by c.orderIndex asc, l.orderIndex asc")
    List<Lesson> findByCourseIdOrdered(@Param("courseId") UUID courseId);

    long countByChapterCourseId(UUID courseId);
}
