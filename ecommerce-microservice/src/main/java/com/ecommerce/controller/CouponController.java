package com.ecommerce.controller;

import com.ecommerce.dto.CouponDTO;
import com.ecommerce.dto.CreateCouponRequest;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.service.CouponService;
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
@RequestMapping("/api/ecommerce/coupons")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Coupons", description = "Coupon management APIs")
public class CouponController {

    private final CouponService couponService;
    private final UserServiceFeignService userServiceFeignService;
    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Get all coupons")
    public ResponseEntity<ApiResponse<List<CouponDTO>>> getAllCoupons() {
        log.info("GET /api/ecommerce/coupons - Fetch all coupons");
        List<CouponDTO> coupons = couponService.getAllCoupons();
        return ResponseEntity.ok(new ApiResponse<>("Coupons retrieved successfully", coupons));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get coupon by ID")
    public ResponseEntity<ApiResponse<CouponDTO>> getCoupon(
        @Parameter(description = "Coupon ID") @PathVariable Long id) {
        log.info("GET /api/ecommerce/coupons/{} - Fetch coupon", id);
        CouponDTO coupon = couponService.getCouponById(id);
        return ResponseEntity.ok(new ApiResponse<>("Coupon retrieved successfully", coupon));
    }

    @PostMapping
    @Operation(summary = "Create new coupon")
    public ResponseEntity<ApiResponse<CouponDTO>> createCoupon(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @Valid @RequestBody CreateCouponRequest request) {
        log.info("POST /api/ecommerce/coupons - Create coupon: {}", request.getCode());
        requireAdmin(authHeader);
        CouponDTO created = couponService.createCoupon(request);
        auditLogService.logAction(authHeader, "COUPON_CREATE", "COUPON", created.getId(),
            "Created coupon '" + created.getCode() + "'");
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>("Coupon created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update coupon")
    public ResponseEntity<ApiResponse<CouponDTO>> updateCoupon(
        @Parameter(description = "Coupon ID") @PathVariable Long id,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @Valid @RequestBody CreateCouponRequest request) {
        log.info("PUT /api/ecommerce/coupons/{} - Update coupon", id);
        requireAdmin(authHeader);
        CouponDTO updated = couponService.updateCoupon(id, request);
        auditLogService.logAction(authHeader, "COUPON_UPDATE", "COUPON", id,
            "Updated coupon '" + updated.getCode() + "'");
        return ResponseEntity.ok(new ApiResponse<>("Coupon updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete coupon")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(
        @Parameter(description = "Coupon ID") @PathVariable Long id,
        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("DELETE /api/ecommerce/coupons/{} - Delete coupon", id);
        requireAdmin(authHeader);
        couponService.deleteCoupon(id);
        auditLogService.logAction(authHeader, "COUPON_DELETE", "COUPON", id,
            "Deleted coupon id=" + id);
        return ResponseEntity.ok(new ApiResponse<>("Coupon deleted successfully", null));
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
