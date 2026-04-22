package com.ecommerce.controller;

import com.ecommerce.dto.SearchKeywordCountDTO;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.service.SearchAnalyticsService;
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
@RequestMapping("/api/ecommerce/analytics")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Analytics", description = "Search analytics APIs")
public class AnalyticsController {

    private final SearchAnalyticsService searchAnalyticsService;
    private final UserServiceFeignService userServiceFeignService;

    @GetMapping("/search/top")
    @Operation(summary = "Top search keywords", description = "Get most searched keywords")
    public ResponseEntity<ApiResponse<List<SearchKeywordCountDTO>>> getTopSearches(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "Lookback window (days)") @RequestParam(defaultValue = "30") int days,
            @Parameter(description = "Max results") @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/ecommerce/analytics/search/top - days={}, limit={}", days, limit);
        requireAdmin(authHeader);
        List<SearchKeywordCountDTO> result = searchAnalyticsService.getTopSearches(days, limit);
        return ResponseEntity.ok(new ApiResponse<>("Top searches retrieved", result));
    }

    @GetMapping("/search/zero-results")
    @Operation(summary = "Zero result searches", description = "Get keywords that returned zero results")
    public ResponseEntity<ApiResponse<List<SearchKeywordCountDTO>>> getZeroResultSearches(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "Lookback window (days)") @RequestParam(defaultValue = "30") int days,
            @Parameter(description = "Max results") @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/ecommerce/analytics/search/zero-results - days={}, limit={}", days, limit);
        requireAdmin(authHeader);
        List<SearchKeywordCountDTO> result = searchAnalyticsService.getZeroResultSearches(days, limit);
        return ResponseEntity.ok(new ApiResponse<>("Zero-result searches retrieved", result));
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
