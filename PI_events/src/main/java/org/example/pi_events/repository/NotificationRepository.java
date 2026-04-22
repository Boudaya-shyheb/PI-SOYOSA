package org.example.pi_events.repository;

import org.example.pi_events.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    long countByUserEmailAndIsReadFalse(String userEmail);
}
