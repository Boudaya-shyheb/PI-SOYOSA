package com.esprit.microservice.trainingservice.repositories;

import com.esprit.microservice.trainingservice.entities.UserPlacementResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserPlacementResultRepository extends JpaRepository<UserPlacementResult, Long> {
    Optional<UserPlacementResult> findByStudentId(Long studentId);
}
