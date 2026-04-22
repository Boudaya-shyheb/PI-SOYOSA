package org.example.pi_events.service;

import lombok.RequiredArgsConstructor;
import org.example.pi_events.DTO.ClubDto;
import org.example.pi_events.entity.Club;
import org.example.pi_events.repository.ClubRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;

    // ==========================
    // CREATE
    // ==========================
    public ClubDto createClub(ClubDto clubDto) {

        Club club = new Club();
        club.setName(clubDto.getName());
        club.setDescription(clubDto.getDescription());
        club.setMeetingLocation(clubDto.getMeetingLocation());
        club.setMeetingSchedule(clubDto.getMeetingSchedule());
        club.setMaxMembers(clubDto.getMaxMembers());

        // ❌ ON NE TOUCHE PAS createdAt
        // Il sera rempli automatiquement avec @PrePersist

        Club savedClub = clubRepository.save(club);

        return toDTO(savedClub);
    }

    // ==========================
    // GET ALL
    // ==========================
    public List<ClubDto> getAllClubs() {
        return clubRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ==========================
    // GET BY ID
    // ==========================
    public ClubDto getClubById(Long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Club not found"));

        return toDTO(club);
    }

    // ==========================
    // UPDATE
    // ==========================
    public ClubDto updateClub(Long id, ClubDto clubDto) {

        Club existingClub = clubRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Club not found"));

        existingClub.setName(clubDto.getName());
        existingClub.setDescription(clubDto.getDescription());
        existingClub.setMeetingLocation(clubDto.getMeetingLocation());
        existingClub.setMeetingSchedule(clubDto.getMeetingSchedule());
        existingClub.setMaxMembers(clubDto.getMaxMembers());

        // ❌ NE PAS modifier createdAt

        Club updatedClub = clubRepository.save(existingClub);

        return toDTO(updatedClub);
    }

    // ==========================
    // DELETE
    // ==========================
    public void deleteClub(Long id) {
        if (!clubRepository.existsById(id)) {
            throw new RuntimeException("Club not found");
        }
        clubRepository.deleteById(id);
    }

    // ==========================
    // MAPPING ENTITY → DTO
    // ==========================
    private ClubDto toDTO(Club club) {
        return new ClubDto(
                club.getId(),
                club.getName(),
                club.getDescription(),
                club.getMeetingLocation(),
                club.getMeetingSchedule(),
                club.getMaxMembers(),
                club.getCreatedAt()
        );
    }
}