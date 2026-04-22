package com.ecommerce.tracking.service;

import org.springframework.stereotype.Service;

import com.ecommerce.tracking.client.OrderServiceClient;
import com.ecommerce.tracking.client.OrderServiceClient.OrderOwnerDto;
import com.ecommerce.tracking.client.OrderServiceClient.OrderCourierDto;

@Service
public class OrderOwnershipService {

    private final OrderServiceClient orderServiceClient;

    public OrderOwnershipService(OrderServiceClient orderServiceClient) {
        this.orderServiceClient = orderServiceClient;
    }

    public OrderOwnerDto requireOrderOwner(String authHeader, Long orderId) {
        try {
            OrderServiceClient.ApiResponse<OrderOwnerDto> response =
                orderServiceClient.getOrderOwner(authHeader, orderId);
            if (response == null || response.getData() == null) {
                return null;
            }
            return response.getData();
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean canSubscribe(String authHeader, String role, String userId, Long orderId) {
        if (role == null || role.isBlank()) {
            return canCourierUpdate(authHeader, userId, orderId);
        }
        if (role.equalsIgnoreCase("ADMIN")) {
            return true;
        }
        if (canCourierUpdate(authHeader, userId, orderId)) {
            return true;
        }
        if (role.equalsIgnoreCase("CUSTOMER")) {
            Long tokenUserId = parseUserId(userId);
            if (tokenUserId == null) {
                return false;
            }
            OrderOwnerDto owner = requireOrderOwner(authHeader, orderId);
            return owner != null && tokenUserId.equals(owner.getCustomerId());
        }
        return false;
    }

    public boolean canCourierUpdate(String authHeader, String userId, Long orderId) {
        Long tokenUserId = parseUserId(userId);
        if (tokenUserId == null) {
            return false;
        }
        OrderCourierDto courier = requireOrderCourier(authHeader, orderId);
        return courier != null && tokenUserId.equals(courier.getCourierId());
    }

    private OrderCourierDto requireOrderCourier(String authHeader, Long orderId) {
        try {
            OrderServiceClient.ApiResponse<OrderCourierDto> response =
                orderServiceClient.getOrderCourier(authHeader, orderId);
            if (response == null || response.getData() == null) {
                return null;
            }
            return response.getData();
        } catch (Exception ex) {
            return null;
        }
    }

    private Long parseUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
