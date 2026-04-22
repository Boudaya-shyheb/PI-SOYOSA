package org.example.pi_events.controller;

import lombok.RequiredArgsConstructor;
import org.example.pi_events.DTO.EventFeedbackDTO;
import org.example.pi_events.service.EventFeedbackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class EventFeedbackController {

    private final EventFeedbackService feedbackService;

    @PostMapping("/{eventId}")
    public EventFeedbackDTO addFeedback(
            @PathVariable Long eventId,
            @RequestBody EventFeedbackDTO dto,
            @RequestHeader(value = "User-Role", required = false) String role) {
        RoleSecurity.requireAnyRole(role, "MEMBER", "VICE_PRESIDENT", "PRESIDENT", "ADMIN");

        return feedbackService.addFeedback(
                eventId,
                dto.getRating(),
                dto.getComment()
        );
    }

    @GetMapping("/event/{eventId}")
    public List<EventFeedbackDTO> getFeedbacksByEvent(@PathVariable Long eventId) {
        return feedbackService.getFeedbacksByEvent(eventId);
    }

    @DeleteMapping("/{id}")
    public void deleteFeedback(
            @PathVariable Long id,
            @RequestHeader(value = "User-Role", required = false) String role) {
        RoleSecurity.requireAnyRole(role, "ADMIN", "PRESIDENT", "VICE_PRESIDENT");
        feedbackService.deleteFeedback(id);
    }
}
