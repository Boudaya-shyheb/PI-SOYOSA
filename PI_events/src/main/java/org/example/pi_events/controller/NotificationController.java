package org.example.pi_events.controller;

import org.example.pi_events.entity.Notification;
import org.example.pi_events.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public List<Notification> getNotifications(@RequestHeader(value = "User-Email", required = false) String emailHeader,
                                             @RequestParam(required = false) String email) {
        String userEmail = (emailHeader != null) ? emailHeader : email;
        if (userEmail == null) return List.of();
        return notificationRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(@RequestHeader(value = "User-Email", required = false) String emailHeader,
                                          @RequestParam(required = false) String email) {
        String userEmail = (emailHeader != null) ? emailHeader : email;
        if (userEmail == null) return Map.of("unreadCount", 0L);
        long count = notificationRepository.countByUserEmailAndIsReadFalse(userEmail);
        return Map.of("unreadCount", count);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
        return ResponseEntity.ok().build();
    }
}
