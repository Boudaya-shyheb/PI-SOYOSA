package com.ecommerce.controller;

import com.ecommerce.dto.OrderDTO;
import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderCourierAssignmentDTO;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.UserServiceFeignService;
import com.ecommerce.service.AuditLogService;
import com.ecommerce.util.ApiResponse;
import com.ecommerce.client.UserServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/ecommerce/orders")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {
    
    private final OrderService orderService;
    private final UserServiceFeignService userServiceFeignService;
    private final AuditLogService auditLogService;
    
    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieve a list of all orders")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getAllOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("GET /api/ecommerce/orders - Fetch all orders");
        requireAdmin(authHeader);
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(new ApiResponse<>("Orders retrieved successfully", orders));
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get all orders with pagination", description = "Retrieve all orders with pagination support")
    public ResponseEntity<ApiResponse<PageResponse<OrderDTO>>> getAllOrdersPaginated(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String direction) {
        log.info("GET /api/ecommerce/orders/paginated - Fetch all orders with pagination");
        requireAdmin(authHeader);
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        PageResponse<OrderDTO> result = orderService.getAllOrdersPaginated(pageable);
        return ResponseEntity.ok(new ApiResponse<>("Orders retrieved successfully", result));
    }

    @GetMapping("/courier-assignments")
    @Operation(summary = "List courier assignments", description = "Retrieve all order courier assignments")
    public ResponseEntity<ApiResponse<List<OrderCourierAssignmentDTO>>> getCourierAssignments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("GET /api/ecommerce/orders/courier-assignments - Fetch courier assignments");
        requireAdmin(authHeader);
        List<OrderCourierAssignmentDTO> assignments = orderService.getAllCourierAssignments();
        return ResponseEntity.ok(new ApiResponse<>("Courier assignments retrieved successfully", assignments));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieve a specific order by its ID")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "Order ID") @PathVariable Long id) {
        log.info("GET /api/ecommerce/orders/{} - Fetch order", id);
        requireAdmin(authHeader);
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(new ApiResponse<>("Order retrieved successfully", order));
    }
    
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get orders by customer", description = "Retrieve all orders for a specific customer")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("GET /api/ecommerce/orders/customer/{} - Fetch orders for customer", customerId);
        requireCustomerAccess(customerId, authHeader);
        List<OrderDTO> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(new ApiResponse<>("Orders retrieved successfully", orders));
    }

    @GetMapping("/customer/{customerId}/paginated")
    @Operation(summary = "Get customer orders with pagination", description = "Retrieve customer orders with pagination support")
    public ResponseEntity<ApiResponse<PageResponse<OrderDTO>>> getOrdersByCustomerPaginated(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/ecommerce/orders/customer/{}/paginated - Fetch customer orders with pagination", customerId);
        requireCustomerAccess(customerId, authHeader);
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<OrderDTO> result = orderService.getOrdersByCustomerPaginated(customerId, pageable);
        return ResponseEntity.ok(new ApiResponse<>("Orders retrieved successfully", result));
    }
    
    @PostMapping
    @Operation(summary = "Create new order", description = "Create a new order")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("POST /api/ecommerce/orders - Create new order for customer: {}", request.getCustomerId());
        Long tokenCustomerId = requireCustomerAccess(request.getCustomerId(), authHeader);
        request.setCustomerId(tokenCustomerId);
        OrderDTO created = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>("Order created successfully", created));
    }
    
    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Update the status of an order")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "New order status") @RequestParam Order.OrderStatus status) {
        log.info("PUT /api/ecommerce/orders/{}/status - Update order status to {}", id, status);
        requireAdmin(authHeader);
        OrderDTO updated = orderService.updateOrderStatus(id, status);
        auditLogService.logAction(authHeader, "ORDER_STATUS_UPDATE", "ORDER", id,
            "Updated order status to " + status);
        return ResponseEntity.ok(new ApiResponse<>("Order status updated successfully", updated));
    }

    @PutMapping("/{id}/assign-courier/{courierId}")
    @Operation(summary = "Assign courier to order", description = "Assign a courier to an order")
    public ResponseEntity<ApiResponse<OrderCourierAssignmentDTO>> assignCourier(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @Parameter(description = "Courier user ID") @PathVariable Long courierId,
            @Parameter(description = "Courier name") @RequestParam(required = false) String courierName,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("PUT /api/ecommerce/orders/{}/assign-courier/{} - Assign courier", id, courierId);
        requireAdmin(authHeader);
        Long adminId = resolveTokenUserId(authHeader);
        OrderCourierAssignmentDTO assignment = orderService.assignCourier(id, courierId, courierName, adminId);
        auditLogService.logAction(authHeader, "ORDER_COURIER_ASSIGN", "ORDER", id,
            "Assigned courierId=" + courierId + (courierName != null ? " courierName=" + courierName : ""));
        return ResponseEntity.ok(new ApiResponse<>("Courier assigned successfully", assignment));
    }

    @DeleteMapping("/{id}/unassign-courier")
    @Operation(summary = "Unassign courier from order", description = "Remove courier assignment from an order")
    public ResponseEntity<ApiResponse<OrderCourierAssignmentDTO>> unassignCourier(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("DELETE /api/ecommerce/orders/{}/unassign-courier - Unassign courier", id);
        requireAdmin(authHeader);
        Long adminId = resolveTokenUserId(authHeader);
        OrderCourierAssignmentDTO assignment = orderService.unassignCourier(id, adminId);
        auditLogService.logAction(authHeader, "ORDER_COURIER_UNASSIGN", "ORDER", id,
            "Unassigned courier");
        return ResponseEntity.ok(new ApiResponse<>("Courier unassigned successfully", assignment));
    }
    
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order and restore stock")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("POST /api/ecommerce/orders/{}/cancel - Cancel order", id);
        requireAdmin(authHeader);
        orderService.cancelOrder(id);
        auditLogService.logAction(authHeader, "ORDER_CANCEL", "ORDER", id,
            "Cancelled order id=" + id);
        return ResponseEntity.ok(new ApiResponse<>("Order cancelled successfully", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete order", description = "Delete an order")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("DELETE /api/ecommerce/orders/{} - Delete order", id);
        requireAdmin(authHeader);
        orderService.deleteOrder(id);
        auditLogService.logAction(authHeader, "ORDER_DELETE", "ORDER", id,
            "Deleted order id=" + id);
        return ResponseEntity.ok(new ApiResponse<>("Order deleted successfully", null));
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

    private Long requireCustomerAccess(Long customerId, String authHeader) {
        requireValidToken(authHeader);
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
            Long tokenCustomerId = Long.valueOf(resolvedUserId);
            if (!tokenCustomerId.equals(customerId)) {
                throw new BusinessException("Customer does not match token");
            }
            return tokenCustomerId;
        } catch (NumberFormatException e) {
            throw new BusinessException("Invalid customer identity");
        }
    }

    private Long resolveTokenUserId(String authHeader) {
        requireValidToken(authHeader);
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
            throw new BusinessException("Invalid user identity");
        }
    }
}
