package com.esprit.microservice.trainingservice.repositories;

import com.esprit.microservice.trainingservice.entities.PlacementTestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlacementTestQuestionRepository extends JpaRepository<PlacementTestQuestion, Long> {
}
