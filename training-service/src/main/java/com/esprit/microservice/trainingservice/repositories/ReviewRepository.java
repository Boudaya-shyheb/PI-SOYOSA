package com.esprit.microservice.trainingservice.repositories;

import com.esprit.microservice.trainingservice.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByTrainingId(int trainingId);
    boolean existsByTrainingIdAndStudentId(int trainingId, Long studentId);
}
