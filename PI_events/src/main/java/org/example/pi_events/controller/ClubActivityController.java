package org.example.pi_events.controller;

import lombok.RequiredArgsConstructor;
import org.example.pi_events.DTO.ClubActivityDto;
import org.example.pi_events.service.ClubActivityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/club-activities")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ClubActivityController {

    private final ClubActivityService activityService;

    @PostMapping
    public ClubActivityDto createActivity(
            @RequestBody ClubActivityDto dto,
            @RequestHeader(value = "User-Role", required = false) String role) {
        RoleSecurity.requireAnyRole(role, "ADMIN", "PRESIDENT", "VICE_PRESIDENT");
        return activityService.createActivity(dto);
    }

    @GetMapping
    public List<ClubActivityDto> getAllActivities() {
        return activityService.getAllActivities();
    }

    @GetMapping("/{id}")
    public ClubActivityDto getActivityById(@PathVariable Long id) {
        return activityService.getActivityById(id);
    }

    @GetMapping("/club/{clubId}")
    public List<ClubActivityDto> getActivitiesByClub(@PathVariable Long clubId) {
        return activityService.getActivitiesByClub(clubId);
    }

    @PutMapping("/{id}")
    public ClubActivityDto updateActivity(
            @PathVariable Long id,
            @RequestBody ClubActivityDto dto,
            @RequestHeader(value = "User-Role", required = false) String role) {
        RoleSecurity.requireAnyRole(role, "ADMIN", "PRESIDENT", "VICE_PRESIDENT");
        return activityService.updateActivity(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteActivity(
            @PathVariable Long id,
            @RequestHeader(value = "User-Role", required = false) String role) {
        RoleSecurity.requireAnyRole(role, "ADMIN", "PRESIDENT");
        activityService.deleteActivity(id);
    }
}
