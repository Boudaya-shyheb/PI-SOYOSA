package org.example.pi_events.repository;

import org.example.pi_events.entity.EventParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventParticipationRepository extends JpaRepository<EventParticipation, Long> {


    List<EventParticipation> findByEventId(Long eventId);
    List<EventParticipation> findByEventIdAndStatusOrderByParticipationDateDesc(Long eventId, String status);
    List<EventParticipation> findByEmailOrderByParticipationDateDesc(String email);
    java.util.Optional<EventParticipation> findByEventIdAndEmail(Long eventId, String email);
    boolean existsByEventIdAndEmail(Long eventId, String email);
    long countByEventIdAndStatus(Long eventId, String status);
}
