package com.ecommerce.controller;

import com.ecommerce.dto.AuditLogDTO;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.service.AuditLogService;
import com.ecommerce.service.UserServiceFeignService;
import com.ecommerce.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ecommerce/audit-logs")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Audit Logs", description = "Admin audit logs")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final UserServiceFeignService userServiceFeignService;

    @GetMapping
    @Operation(summary = "Get recent audit logs", description = "Fetch recent admin actions")
    public ResponseEntity<ApiResponse<List<AuditLogDTO>>> getRecentLogs(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "Max results") @RequestParam(defaultValue = "25") int limit) {
        log.info("GET /api/ecommerce/audit-logs - limit={}", limit);
        requireAdmin(authHeader);
        List<AuditLogDTO> logs = auditLogService.getRecent(limit);
        return ResponseEntity.ok(new ApiResponse<>("Audit logs retrieved", logs));
    }

    private void requireValidToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new BusinessException("Authorization header required");
        }
        if (!userServiceFeignService.validateToken(authHeader)) {
            throw new BusinessException("Invalid or expired token");
        }
    }

    private void requireAdmin(String authHeader) {
        requireValidToken(authHeader);
        String role = userServiceFeignService.getUserRole(authHeader);
        if (role == null || role.isBlank()) {
            throw new BusinessException("Invalid user role");
        }
        if (!"ADMIN".equals(role)) {
            throw new BusinessException("Admin privileges required");
        }
    }
}
