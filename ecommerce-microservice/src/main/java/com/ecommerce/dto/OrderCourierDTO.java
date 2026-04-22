package com.ecommerce.dto;

public class OrderCourierDTO {
    private Long orderId;
    private Long courierId;

    public OrderCourierDTO() {
    }

    public OrderCourierDTO(Long orderId, Long courierId) {
        this.orderId = orderId;
        this.courierId = courierId;
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
}
