package com.esprit.microservice.trainingservice.repositories;

import com.esprit.microservice.trainingservice.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    long countByStudentIdAndIsReadFalse(Long studentId);
}
