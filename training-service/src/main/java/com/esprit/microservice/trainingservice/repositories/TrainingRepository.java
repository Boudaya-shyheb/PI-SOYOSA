package com.esprit.microservice.trainingservice.repositories;

import com.esprit.microservice.trainingservice.entities.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Integer> {
    java.util.List<Training> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
    org.springframework.data.domain.Page<Training> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description, org.springframework.data.domain.Pageable pageable);
    
    java.util.List<Training> findByLevel(com.esprit.microservice.trainingservice.entities.Level level);
}
