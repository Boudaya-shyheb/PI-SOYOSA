package com.englishway.course.controller;

import com.englishway.course.dto.*;
import com.englishway.course.service.CertificateService;
import com.englishway.course.util.RequestContext;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {
    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<byte[]> downloadCertificate(
        @PathVariable("courseId") UUID courseId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        return certificateService.generateCertificate(courseId, context.getUserId());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> issueCertificate(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody Map<String, UUID> request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        return certificateService.issueCertificate(context, request.get("courseId"));
    }

    @PutMapping("/{certificateId}")
    public ResponseEntity<Map<String, Object>> updateCertificate(
        @PathVariable("certificateId") UUID certificateId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody Map<String, String> request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        return certificateService.updateCertificate(context, certificateId, request);
    }

    @DeleteMapping("/{certificateId}")
    public ResponseEntity<String> revokeCertificate(
        @PathVariable("certificateId") UUID certificateId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        certificateService.revokeCertificate(context, certificateId);
        return ResponseEntity.ok("Certificate revoked successfully");
    }
}
