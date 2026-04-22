package org.example.pi_events.repository;

import org.example.pi_events.entity.EventFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventFeedbackRepository extends JpaRepository<EventFeedback, Long> {


    List<EventFeedback> findByEventId(Long eventId);
}
