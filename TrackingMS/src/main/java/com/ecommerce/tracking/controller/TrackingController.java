package com.ecommerce.tracking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;

import com.ecommerce.tracking.dto.LocationUpdate;
import com.ecommerce.tracking.service.LocationStore;
import com.ecommerce.tracking.service.OrderOwnershipService;
import com.ecommerce.tracking.service.UserServiceFeignService;

@RestController
@RequestMapping("/tracking")
public class TrackingController {

    private final SimpMessagingTemplate messaging;
    private final LocationStore store;
    private final UserServiceFeignService userServiceFeignService;
    private final OrderOwnershipService orderOwnershipService;

    public TrackingController(SimpMessagingTemplate messaging, LocationStore store,
                              UserServiceFeignService userServiceFeignService,
                              OrderOwnershipService orderOwnershipService) {
        this.messaging = messaging;
        this.store = store;
        this.userServiceFeignService = userServiceFeignService;
        this.orderOwnershipService = orderOwnershipService;
    }

    @PostMapping("/update")
    public ResponseEntity<Void> update(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                       @RequestBody LocationUpdate dto) {
        requireCourierAccess(authHeader, dto.getOrderId());
        store.saveLatest(dto.getOrderId(), dto);
        messaging.convertAndSend("/topic/orders/" + dto.getOrderId(), dto);
        return ResponseEntity.ok().build();
    }

    private void requireCourierAccess(String authHeader, Long orderId) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header required");
        }
        if (orderId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order ID is required");
        }
        if (!userServiceFeignService.validateToken(authHeader)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        String role = userServiceFeignService.getUserRole(authHeader);
        if ("ADMIN".equalsIgnoreCase(role)) {
            return;
        }
        String userId = userServiceFeignService.getUserId(authHeader);
        if (!orderOwnershipService.canCourierUpdate(authHeader, userId, orderId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Courier not assigned to this order");
        }
    }
}
