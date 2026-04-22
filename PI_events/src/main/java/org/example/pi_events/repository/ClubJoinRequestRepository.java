package org.example.pi_events.repository;

import org.example.pi_events.entity.ClubJoinRequest;
import org.example.pi_events.entity.ClubJoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubJoinRequestRepository extends JpaRepository<ClubJoinRequest, Long> {
    Optional<ClubJoinRequest> findByClubIdAndEmail(Long clubId, String email);
    List<ClubJoinRequest> findByClubIdAndStatusOrderByRequestedAtDesc(Long clubId, ClubJoinRequestStatus status);
    List<ClubJoinRequest> findByEmailOrderByRequestedAtDesc(String email);
}
