package com.ecommerce.dto;

public class OrderOwnerDTO {
    private Long orderId;
    private Long customerId;

    public OrderOwnerDTO() {
    }

    public OrderOwnerDTO(Long orderId, Long customerId) {
        this.orderId = orderId;
        this.customerId = customerId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
