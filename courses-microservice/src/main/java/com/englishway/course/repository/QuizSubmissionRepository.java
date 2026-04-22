package com.englishway.course.repository;

import com.englishway.course.entity.QuizSubmission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, UUID> {
    Optional<QuizSubmission> findTopByQuizIdAndStudentIdOrderBySubmittedAtDesc(UUID quizId, String studentId);

    long countByQuizIdAndStudentId(UUID quizId, String studentId);

    List<QuizSubmission> findByQuizIdOrderBySubmittedAtDesc(UUID quizId);

    List<QuizSubmission> findByStudentIdOrderBySubmittedAtDesc(String studentId);

    List<QuizSubmission> findTop10ByQuizIdOrderByScoreDesc(UUID quizId);
}
