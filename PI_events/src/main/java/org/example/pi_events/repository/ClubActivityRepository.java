package org.example.pi_events.repository;

import org.example.pi_events.entity.ClubActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubActivityRepository extends JpaRepository<ClubActivity, Long> {
    List<ClubActivity> findByClubId(Long clubId);
}
