package com.ecommerce.controller;

import com.ecommerce.dto.BundleDTO;
import com.ecommerce.dto.CreateBundleRequest;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.service.BundleService;
import com.ecommerce.service.UserServiceFeignService;
import com.ecommerce.service.AuditLogService;
import com.ecommerce.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/ecommerce/bundles")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Bundles", description = "Bundle management APIs")
public class BundleController {
    
    private final BundleService bundleService;
    private final UserServiceFeignService userServiceFeignService;
    private final AuditLogService auditLogService;
    
    @GetMapping
    @Operation(summary = "Get all bundles", description = "Retrieve a list of all bundles")
    public ResponseEntity<ApiResponse<List<BundleDTO>>> getAllBundles() {
        log.info("GET /api/ecommerce/bundles - Fetch all bundles");
        List<BundleDTO> bundles = bundleService.getAllBundles();
        return ResponseEntity.ok(new ApiResponse<>("Bundles retrieved successfully", bundles));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get bundle by ID", description = "Retrieve a specific bundle by its ID")
    public ResponseEntity<ApiResponse<BundleDTO>> getBundle(
        @Parameter(description = "Bundle ID") @PathVariable Long id) {
        log.info("GET /api/ecommerce/bundles/{} - Fetch bundle", id);
        BundleDTO bundle = bundleService.getBundleById(id);
        return ResponseEntity.ok(new ApiResponse<>("Bundle retrieved successfully", bundle));
    }
    
    @PostMapping
    @Operation(summary = "Create new bundle", description = "Create a new bundle")
    public ResponseEntity<ApiResponse<BundleDTO>> createBundle(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @Valid @RequestBody CreateBundleRequest request) {
        log.info("POST /api/ecommerce/bundles - Create new bundle: {}", request.getName());
        requireAdmin(authHeader);
        BundleDTO created = bundleService.createBundle(request);
        auditLogService.logAction(authHeader, "BUNDLE_CREATE", "BUNDLE", created.getId(),
            "Created bundle '" + created.getName() + "'");
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>("Bundle created successfully", created));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update bundle", description = "Update an existing bundle")
    public ResponseEntity<ApiResponse<BundleDTO>> updateBundle(
        @Parameter(description = "Bundle ID") @PathVariable Long id,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @Valid @RequestBody CreateBundleRequest request) {
        log.info("PUT /api/ecommerce/bundles/{} - Update bundle", id);
        requireAdmin(authHeader);
        BundleDTO updated = bundleService.updateBundle(id, request);
        auditLogService.logAction(authHeader, "BUNDLE_UPDATE", "BUNDLE", id,
            "Updated bundle '" + updated.getName() + "'");
        return ResponseEntity.ok(new ApiResponse<>("Bundle updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete bundle", description = "Delete a bundle")
    public ResponseEntity<ApiResponse<Void>> deleteBundle(
        @Parameter(description = "Bundle ID") @PathVariable Long id,
        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("DELETE /api/ecommerce/bundles/{} - Delete bundle", id);
        requireAdmin(authHeader);
        bundleService.deleteBundle(id);
        auditLogService.logAction(authHeader, "BUNDLE_DELETE", "BUNDLE", id,
            "Deleted bundle id=" + id);
        return ResponseEntity.ok(new ApiResponse<>("Bundle deleted successfully", null));
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
