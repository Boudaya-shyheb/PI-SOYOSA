package org.example.pi_events.controller;

import lombok.RequiredArgsConstructor;
import org.example.pi_events.DTO.QuizQuestionDto;
import org.example.pi_events.DTO.QuizSubmitDto;
import org.example.pi_events.service.ClubJoinQuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/club-members/join-quiz")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ClubJoinQuizController {

    private static final String AUTH_DISABLED_DEFAULT_EMAIL = "admin@pi-event.tn";
    private final ClubJoinQuizService clubJoinQuizService;

    @GetMapping("/{clubId}")
    public List<QuizQuestionDto> getQuizQuestions(@PathVariable Long clubId) {
        return clubJoinQuizService.getQuizQuestions();
    }

    @GetMapping("/passing-score")
    public ResponseEntity<Map<String, Integer>> getPassingScore() {
        return ResponseEntity.ok(Map.of("passingScore", clubJoinQuizService.getPassingScore()));
    }

    @PutMapping("/passing-score")
    public ResponseEntity<Map<String, Integer>> updatePassingScore(
            @RequestBody Map<String, Integer> payload,
            @RequestHeader(value = "User-Role", required = false) String role) {
        RoleSecurity.requireAnyRole(role, "ADMIN", "PRESIDENT");
        Integer passingScore = payload.get("passingScore");
        if (passingScore == null) {
            throw new IllegalArgumentException("passingScore is required");
        }
        clubJoinQuizService.updatePassingScore(passingScore);
        return ResponseEntity.ok(Map.of("passingScore", clubJoinQuizService.getPassingScore()));
    }

    @PostMapping("/{clubId}/evaluate")
    public ResponseEntity<Map<String, String>> evaluateQuizAndJoin(
            @PathVariable Long clubId,
            @RequestBody QuizSubmitDto dto,
            Principal principal,
            @RequestHeader(value = "User-Role", required = false) String role,
            @RequestParam(required = false) String email) {
        RoleSecurity.requireAnyRole(role, "MEMBER", "VICE_PRESIDENT", "PRESIDENT", "ADMIN");
        try {
            String message = clubJoinQuizService.evaluateQuizAndJoin(
                    clubId,
                    resolveEmail(principal, email),
                    dto.getAnswers(),
                    dto.getQuestionIds()
            );
            return ResponseEntity.ok(Map.of("message", message));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
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
