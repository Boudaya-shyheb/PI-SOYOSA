package com.ecommerce.dto;

import java.time.LocalDateTime;

public class OrderCourierAssignmentDTO {
    private Long orderId;
    private Long courierId;
    private String courierName;
    private Long assignedBy;
    private LocalDateTime assignedAt;

    public OrderCourierAssignmentDTO() {
    }

    public OrderCourierAssignmentDTO(Long orderId, Long courierId, String courierName, Long assignedBy, LocalDateTime assignedAt) {
        this.orderId = orderId;
        this.courierId = courierId;
        this.courierName = courierName;
        this.assignedBy = assignedBy;
        this.assignedAt = assignedAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getCourierId() {
        return courierId;
    }

    public void setCourierId(Long courierId) {
        this.courierId = courierId;
    }

    public String getCourierName() {
        return courierName;
    }

    public void setCourierName(String courierName) {
        this.courierName = courierName;
    }

    public Long getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(Long assignedBy) {
        this.assignedBy = assignedBy;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
