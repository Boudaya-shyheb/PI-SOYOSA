package com.englishway.course.repository;

import com.englishway.course.entity.Quiz;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    Optional<Quiz> findByLessonId(UUID lessonId);
    
    @Query("SELECT q FROM Quiz q WHERE q.lesson.chapter.course.id = :courseId")
    List<Quiz> findByCourseId(@Param("courseId") UUID courseId);
    
    boolean existsByLessonId(UUID lessonId);
    
    void deleteByLessonId(UUID lessonId);
}
