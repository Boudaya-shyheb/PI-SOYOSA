package com.englishway.course.repository;

import com.englishway.course.entity.LearningMaterial;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LearningMaterialRepository extends JpaRepository<LearningMaterial, UUID> {
    List<LearningMaterial> findByLessonId(UUID lessonId);

    @Query("select count(m) from LearningMaterial m join m.lesson l join l.chapter c where c.course.id = :courseId")
    long countByCourseId(@Param("courseId") UUID courseId);

    @Query("select case when count(m) > 0 then true else false end from LearningMaterial m join m.lesson l join l.chapter c where c.course.id = :courseId")
    boolean existsContentForCourse(@Param("courseId") UUID courseId);
}
