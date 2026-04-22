package org.example.pi_events.controller;

import lombok.RequiredArgsConstructor;
import org.example.pi_events.DTO.EventParticipationDTO;
import org.example.pi_events.service.EventParticipationService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/participations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class EventParticipationController {

    private static final String AUTH_DISABLED_DEFAULT_EMAIL = "admin@pi-event.tn";
    private final EventParticipationService participationService;

    // 🔥 Create participation (with full form data)
    @PostMapping("/{eventId}")
    public EventParticipationDTO participate(
            @PathVariable Long eventId,
            @RequestBody EventParticipationDTO dto,
            Principal principal,
            @RequestHeader(value = "User-Role", required = false) String role,
            @RequestParam(required = false) String email) {
        RoleSecurity.requireAnyRole(role, "MEMBER", "VICE_PRESIDENT", "PRESIDENT", "ADMIN");

        return participationService.participate(eventId, dto, resolveEmail(principal, email));
    }

    // 🔥 Get all participants for one event
    @GetMapping("/event/{eventId}")
    public List<EventParticipationDTO> getParticipants(
            @PathVariable Long eventId) {

        return participationService.getParticipationsByEvent(eventId);
    }

    @GetMapping("/requests/pending/event/{eventId}")
    public List<EventParticipationDTO> getPendingRequestsByEvent(
            @PathVariable Long eventId,
            @RequestHeader(value = "User-Role", required = false) String role) {
        RoleSecurity.requireAnyRole(role, "VICE_PRESIDENT", "PRESIDENT", "ADMIN");
        return participationService.getPendingRequestsByEvent(eventId);
    }

    @GetMapping("/requests/me")
    public List<EventParticipationDTO> getMyRequestStatuses(
            Principal principal,
            @RequestHeader(value = "User-Role", required = false) String role,
            @RequestParam(required = false) String email
    ) {
        RoleSecurity.requireAnyRole(role, "MEMBER", "VICE_PRESIDENT", "PRESIDENT", "ADMIN");
        return participationService.getMyParticipationRequests(resolveEmail(principal, email));
    }

    @PutMapping("/requests/{id}/approve")
    public EventParticipationDTO approveRequest(
            @PathVariable Long id,
            @RequestHeader(value = "User-Role", required = false) String role) {
        RoleSecurity.requireAnyRole(role, "VICE_PRESIDENT", "PRESIDENT", "ADMIN");
        return participationService.approveParticipationRequest(id);
    }

    @PutMapping("/requests/{id}/reject")
    public EventParticipationDTO rejectRequest(
            @PathVariable Long id,
            @RequestHeader(value = "User-Role", required = false) String role) {
        RoleSecurity.requireAnyRole(role, "VICE_PRESIDENT", "PRESIDENT", "ADMIN");
        return participationService.rejectParticipationRequest(id);
    }

    // 🔥 Cancel participation
    @DeleteMapping("/{id}")
    public void cancelParticipation(
            @PathVariable Long id,
            @RequestHeader(value = "User-Role", required = false) String role) {
        RoleSecurity.requireAnyRole(role, "MEMBER", "VICE_PRESIDENT", "PRESIDENT", "ADMIN");
        participationService.cancelParticipation(id);
    }
    // 🔥 GET ONE PARTICIPANT BY ID
    @GetMapping("/{id}")
    public EventParticipationDTO getParticipationById(@PathVariable Long id) {
        return participationService.getParticipationById(id);
    }

    private String resolveEmail(Principal principal, String email) {
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            return principal.getName();
        }
        if (email != null && !email.isBlank()) {
            return email;
        }
        return AUTH_DISABLED_DEFAULT_EMAIL;
    }
}
