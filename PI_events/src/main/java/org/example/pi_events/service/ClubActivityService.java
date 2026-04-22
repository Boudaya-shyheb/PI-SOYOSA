package org.example.pi_events.service;

import lombok.RequiredArgsConstructor;
import org.example.pi_events.DTO.ClubActivityDto;
import org.example.pi_events.entity.Club;
import org.example.pi_events.entity.ClubActivity;
import org.example.pi_events.repository.ClubActivityRepository;
import org.example.pi_events.repository.ClubRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubActivityService {

    private final ClubActivityRepository activityRepository;
    private final ClubRepository clubRepository;

    public ClubActivityDto createActivity(ClubActivityDto dto) {
        Club club = clubRepository.findById(dto.getClubId())
                .orElseThrow(() -> new RuntimeException("Club not found"));

        ClubActivity activity = new ClubActivity();
        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        activity.setActivityDate(dto.getActivityDate());
        activity.setLocation(dto.getLocation());
        activity.setClub(club);

        return toDTO(activityRepository.save(activity));
    }

    public List<ClubActivityDto> getAllActivities() {
        return activityRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public ClubActivityDto getActivityById(Long id) {
        ClubActivity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        return toDTO(activity);
    }

    public List<ClubActivityDto> getActivitiesByClub(Long clubId) {
        return activityRepository.findByClubId(clubId).stream()
                .map(this::toDTO)
                .toList();
    }

    public ClubActivityDto updateActivity(Long id, ClubActivityDto dto) {
        ClubActivity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        activity.setActivityDate(dto.getActivityDate());
        activity.setLocation(dto.getLocation());

        Long clubId = dto.getClubId();
        if (clubId != null && (activity.getClub() == null || !clubId.equals(activity.getClub().getId()))) {
            Club club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new RuntimeException("Club not found"));
            activity.setClub(club);
        }

        return toDTO(activityRepository.save(activity));
    }

    public void deleteActivity(Long id) {
        activityRepository.deleteById(id);
    }

    private ClubActivityDto toDTO(ClubActivity activity) {
        return new ClubActivityDto(
                activity.getId(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getActivityDate(),
                activity.getLocation(),
                activity.getClub() != null ? activity.getClub().getId() : null
        );
    }
}
