package org.example.pi_events.repository;

import org.example.pi_events.entity.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    long countByClubId(Long clubId);
    boolean existsByClubIdAndEmail(Long clubId, String email);
}
