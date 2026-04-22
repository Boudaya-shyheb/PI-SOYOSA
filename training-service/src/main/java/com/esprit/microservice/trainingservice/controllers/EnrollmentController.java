package com.esprit.microservice.trainingservice.controllers;

import com.esprit.microservice.trainingservice.entities.Enrollment;
import com.esprit.microservice.trainingservice.security.SecurityUser;
import com.esprit.microservice.trainingservice.services.IEnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollment")
@CrossOrigin("*")
public class EnrollmentController {

    @Autowired
    private IEnrollmentService enrollmentService;

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @PostMapping("/training/{trainingId}")
    public ResponseEntity<?> buyTraining(@PathVariable int trainingId, @AuthenticationPrincipal SecurityUser user) {
        try {
            Enrollment enrollment = enrollmentService.buyTraining(trainingId, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @PostMapping("/training/{trainingId}/session/{sessionId}")
    public ResponseEntity<?> enrollInSession(
            @PathVariable int trainingId,
            @PathVariable int sessionId,
            @AuthenticationPrincipal SecurityUser user) {
        try {
            Enrollment enrollment = enrollmentService.enrollInSession(trainingId, sessionId, user);
            return ResponseEntity.ok(enrollment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @GetMapping("/my-enrollments")
    public ResponseEntity<org.springframework.data.domain.Page<Enrollment>> getMyEnrollments(
            @AuthenticationPrincipal SecurityUser user,
            org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(enrollmentService.getStudentEnrollments(user, pageable));
    }
    
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @GetMapping("/training/{trainingId}/status")
    public ResponseEntity<Map<String, Boolean>> getEnrollmentStatus(@PathVariable int trainingId, @AuthenticationPrincipal SecurityUser user) {
        boolean isEnrolled = enrollmentService.isStudentEnrolledInTraining(trainingId, user);
        boolean isEligible = enrollmentService.isEligibleToReview(trainingId, user);
        Map<String, Boolean> response = new HashMap<>();
        response.put("enrolled", isEnrolled);
        response.put("eligibleToReview", isEligible);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('ROLE_TUTOR')")
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<Enrollment>> getSessionEnrollments(@PathVariable int sessionId) {
        return ResponseEntity.ok(enrollmentService.getSessionEnrollments(sessionId));
    }

    @PreAuthorize("hasAuthority('ROLE_TUTOR')")
    @PatchMapping("/{enrollmentId}/presence")
    public ResponseEntity<?> togglePresence(
            @PathVariable int enrollmentId,
            @RequestParam boolean present) {
        try {
            Enrollment updated = enrollmentService.togglePresence(enrollmentId, present);
            Map<String, Object> response = new HashMap<>();
            response.put("id", updated.getId());
            response.put("present", updated.isPresent());
            response.put("studentId", updated.getStudentId());
            response.put("certificateIssued", updated.isCertificateIssued());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.toString() + " - " + (e.getCause() != null ? e.getCause().toString() : ""));
        }
    }

    @PreAuthorize("hasAuthority('ROLE_TUTOR')")
    @PostMapping("/{enrollmentId}/issue-certificate")
    public ResponseEntity<?> issueCertificate(@PathVariable int enrollmentId) {
        try {
            Enrollment updated = enrollmentService.issueCertificate(enrollmentId);
            Map<String, Object> response = new HashMap<>();
            response.put("id", updated.getId());
            response.put("present", updated.isPresent());
            response.put("studentId", updated.getStudentId());
            response.put("certificateIssued", updated.isCertificateIssued());
            response.put("certificateIssuedDate", updated.getCertificateIssuedDate());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.toString() + " - " + (e.getCause() != null ? e.getCause().toString() : ""));
        }
    }

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @GetMapping("/upcoming")
    public ResponseEntity<List<Enrollment>> getUpcomingEnrollments(@AuthenticationPrincipal SecurityUser student) {
        return ResponseEntity.ok(enrollmentService.getUpcomingEnrollments(student));
    }

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @GetMapping("/my-certificates")
    public ResponseEntity<List<Enrollment>> getMyCertificates(@AuthenticationPrincipal SecurityUser student) {
        return ResponseEntity.ok(enrollmentService.getStudentCertificates(student));
    }

   /* @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @DeleteMapping("/{enrollmentId}")
    public ResponseEntity<?> cancelEnrollment(
            @PathVariable int enrollmentId,
            @AuthenticationPrincipal SecurityUser user) {
        try {
            enrollmentService.cancelEnrollment(enrollmentId, user);
            return ResponseEntity.ok("Enrollment cancelled successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }*/
}
