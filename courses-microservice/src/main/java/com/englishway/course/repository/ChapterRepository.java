package com.englishway.course.repository;

import com.englishway.course.entity.Chapter;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChapterRepository extends JpaRepository<Chapter, UUID> {
    List<Chapter> findByCourseIdOrderByOrderIndex(UUID courseId);

    long countByCourseId(UUID courseId);
}
