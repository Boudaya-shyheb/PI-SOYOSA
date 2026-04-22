package com.ecommerce.controller;

import com.ecommerce.dto.OrderDTO;
import com.ecommerce.dto.OrderCourierAssignmentDTO;
import com.ecommerce.dto.OrderCourierDTO;
import com.ecommerce.dto.OrderOwnerDTO;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.UserServiceFeignService;
import com.ecommerce.util.ApiResponse;
import com.ecommerce.client.UserServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Internal Orders", description = "Internal order ownership checks")
public class InternalOrderController {

    private final OrderService orderService;
    private final UserServiceFeignService userServiceFeignService;

    @GetMapping("/{id}/owner")
    @Operation(summary = "Get order owner", description = "Internal endpoint for ownership checks")
    public ResponseEntity<ApiResponse<OrderOwnerDTO>> getOrderOwner(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @PathVariable Long id) {
        requireValidToken(authHeader);
        String role = userServiceFeignService.getUserRole(authHeader);
        if (role == null || role.isBlank()) {
            throw new BusinessException("Invalid user role");
        }
        OrderDTO order = orderService.getOrderById(id);
        if (order == null || order.getCustomerId() == null) {
            throw new BusinessException("Order not found");
        }
        if ("ADMIN".equals(role)) {
            return ResponseEntity.ok(new ApiResponse<>("Order owner resolved", new OrderOwnerDTO(id, order.getCustomerId())));
        }
        if ("CUSTOMER".equals(role)) {
            Long tokenCustomerId = resolveTokenUserId(authHeader);
            if (!order.getCustomerId().equals(tokenCustomerId)) {
                throw new BusinessException("Customer does not match order");
            }
            return ResponseEntity.ok(new ApiResponse<>("Order owner resolved", new OrderOwnerDTO(id, order.getCustomerId())));
        }
        if ("COURIER".equals(role)) {
            return ResponseEntity.ok(new ApiResponse<>("Order owner resolved", new OrderOwnerDTO(id, order.getCustomerId())));
        }
        throw new BusinessException("Access denied for role");
    }

    @GetMapping("/{id}/courier")
    @Operation(summary = "Get order courier", description = "Internal endpoint for courier assignment checks")
    public ResponseEntity<ApiResponse<OrderCourierDTO>> getOrderCourier(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @PathVariable Long id) {
        requireValidToken(authHeader);
        String role = userServiceFeignService.getUserRole(authHeader);
        if (role == null || role.isBlank()) {
            throw new BusinessException("Invalid user role");
        }

        OrderCourierAssignmentDTO assignment = orderService.getCourierAssignment(id);
        if (assignment == null || assignment.getCourierId() == null) {
            throw new BusinessException("Courier assignment not found");
        }

        Long tokenUserId = resolveTokenUserId(authHeader);

        if ("ADMIN".equals(role)) {
            return ResponseEntity.ok(new ApiResponse<>("Order courier resolved",
                new OrderCourierDTO(id, assignment.getCourierId())));
        }

        if (assignment.getCourierId().equals(tokenUserId)) {
            return ResponseEntity.ok(new ApiResponse<>("Order courier resolved",
                new OrderCourierDTO(id, assignment.getCourierId())));
        }

        if ("CUSTOMER".equals(role)) {
            OrderDTO order = orderService.getOrderById(id);
            if (!order.getCustomerId().equals(tokenUserId)) {
                throw new BusinessException("Customer does not match order");
            }
            return ResponseEntity.ok(new ApiResponse<>("Order courier resolved",
                new OrderCourierDTO(id, assignment.getCourierId())));
        }

        throw new BusinessException("Access denied for role");
    }

    private void requireValidToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new BusinessException("Authorization header required");
        }
        if (!userServiceFeignService.validateToken(authHeader)) {
            throw new BusinessException("Invalid or expired token");
        }
    }

    private Long resolveTokenUserId(String authHeader) {
        String userKey = userServiceFeignService.getUserId(authHeader);
        if (userKey == null || userKey.isBlank()) {
            throw new BusinessException("Invalid user identity");
        }
        UserServiceClient.UserInfoDto info = userServiceFeignService.getUserInfo(userKey);
        String resolvedUserId = info != null ? info.getUserId() : null;
        if (resolvedUserId == null || resolvedUserId.isBlank()) {
            throw new BusinessException("Invalid user identity");
        }
        try {
            return Long.valueOf(resolvedUserId);
        } catch (NumberFormatException e) {
            throw new BusinessException("Invalid customer identity");
        }
    }
}
