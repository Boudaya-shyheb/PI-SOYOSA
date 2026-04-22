package com.esprit.microservice.trainingservice.controllers;

import com.esprit.microservice.trainingservice.dto.TrainingCreateDTO;
import com.esprit.microservice.trainingservice.dto.TrainingUpdateDTO;
import com.esprit.microservice.trainingservice.entities.Training;
import com.esprit.microservice.trainingservice.security.SecurityUser;
import com.esprit.microservice.trainingservice.services.ITrainingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.esprit.microservice.trainingservice.dto.SessionDTO;
import com.esprit.microservice.trainingservice.entities.Session;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.esprit.microservice.trainingservice.client.UserClient;
import com.esprit.microservice.trainingservice.dto.UserInfoDto;

import java.util.List;

@RestController
@RequestMapping("/api/training")
@CrossOrigin("*")
public class TrainingService {

    @Autowired
    private ITrainingService trainingService;

    @Autowired
    private UserClient userClient;

    @Autowired
    private com.esprit.microservice.trainingservice.services.CloudinaryService cloudinaryService;

    @PreAuthorize("hasAuthority('ROLE_TUTOR')")
    @PostMapping
    public ResponseEntity<Training> createTraining(
            @Valid @RequestBody TrainingCreateDTO dto,
            @AuthenticationPrincipal SecurityUser user) {

        Training training = trainingService.addTraining(dto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(training);
    }

    @PreAuthorize("hasAuthority('ROLE_TUTOR')")
    @PostMapping("/image-upload")
    public ResponseEntity<java.util.Map<String, String>> uploadImage(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            String url = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(java.util.Map.of("url", url));
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasAuthority('ROLE_TUTOR')")
    @PutMapping("{id}")
    public Training updateTraining(
            @PathVariable int id,
            @Valid @RequestBody TrainingUpdateDTO training,
            @AuthenticationPrincipal SecurityUser user) {
        return trainingService.updateTraining(id, training, user);
    }

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<Training>> getTrainings(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) String search,
            org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(trainingService.getTrainings(user, search, pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<Training> getTraining(@PathVariable int id) {
        return ResponseEntity.ok(trainingService.getTraining(id));
    }

    @PreAuthorize("hasAuthority('ROLE_TUTOR')")
    @DeleteMapping("{id}")
    public void deleteTraining(@PathVariable int id, @AuthenticationPrincipal SecurityUser user) {
        trainingService.deleteTraining(id, user);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<Training>> getRecommendations(@AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.ok(trainingService.getRecommendations(user));
    }

    // ========== SESSION ENDPOINTS ==========

    @PreAuthorize("hasAuthority('ROLE_TUTOR')")
    @PostMapping("{trainingId}/sessions")
    public ResponseEntity<?> addSession(
            @PathVariable int trainingId,
            @Valid @RequestBody SessionDTO sessionDTO,
            @AuthenticationPrincipal SecurityUser user) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(trainingService.addSession(trainingId, sessionDTO, user));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", e.getMessage(), "type", e.getClass().getName()));
        }
    }

    @GetMapping("{trainingId}/sessions")
    public ResponseEntity<org.springframework.data.domain.Page<Session>> getSessions(
            @PathVariable int trainingId,
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.util.Date startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.util.Date endDate,
            org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(trainingService.getSessionsByTraining(trainingId, user, startDate, endDate, pageable));
    }

    @PreAuthorize("hasAuthority('ROLE_TUTOR')")
    @PutMapping("{trainingId}/sessions/{sessionId}")
    public ResponseEntity<Session> updateSession(
            @PathVariable int trainingId,
            @PathVariable int sessionId,
            @Valid @RequestBody SessionDTO sessionDTO,
            @AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.ok(trainingService.updateSession(trainingId, sessionId, sessionDTO, user));
    }

    @PreAuthorize("hasAuthority('ROLE_TUTOR')")
    @DeleteMapping("{trainingId}/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable int trainingId,
            @PathVariable int sessionId,
            @AuthenticationPrincipal SecurityUser user) {
        trainingService.deleteSession(trainingId, sessionId, user);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('ROLE_TUTOR')")
    @PutMapping("{trainingId}/sessions/{sessionId}/complete")
    public ResponseEntity<Session> completeSession(
            @PathVariable int trainingId,
            @PathVariable int sessionId,
            @AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.ok(trainingService.markSessionAsCompleted(trainingId, sessionId, user));
    }

    // ========== REVIEW ENDPOINTS ==========

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @PostMapping("/{trainingId}/reviews")
    public ResponseEntity<com.esprit.microservice.trainingservice.entities.Review> addReview(
            @PathVariable int trainingId,
            @Valid @RequestBody com.esprit.microservice.trainingservice.dto.ReviewDTO reviewDTO,
            @AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.ok(trainingService.addReview(trainingId, reviewDTO, user));
    }

    @GetMapping("/{trainingId}/reviews")
    public ResponseEntity<List<com.esprit.microservice.trainingservice.dto.ReviewDTO>> getReviews(@PathVariable int trainingId) {
        List<com.esprit.microservice.trainingservice.entities.Review> reviews = trainingService.getReviewsByTraining(trainingId);
        List<com.esprit.microservice.trainingservice.dto.ReviewDTO> dtos = reviews.stream().map(r -> {
            com.esprit.microservice.trainingservice.dto.ReviewDTO dto = new com.esprit.microservice.trainingservice.dto.ReviewDTO();
            dto.setId(r.getId());
            dto.setRating(r.getRating());
            dto.setComment(r.getComment());
            dto.setTrainingId(r.getTraining().getId());
            dto.setStudentId(r.getStudentId());
            
            String studentName = "Student #" + r.getStudentId();
            try {
                UserInfoDto userInfo = userClient.getUserInfo(String.valueOf(r.getStudentId()));
                if (userInfo != null) {
                    studentName = userInfo.getDisplayName();
                }
            } catch (Exception e) {
                // Ignore fallback to default
            }
            dto.setStudentName(studentName); 
            dto.setCreatedAt(r.getCreatedAt());
            return dto;
        }).toList();
        return ResponseEntity.ok(dtos);
    }
}
