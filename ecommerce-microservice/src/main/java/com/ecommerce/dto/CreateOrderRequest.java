package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be positive")
    private Long customerId;

    @NotEmpty(message = "Order must contain at least one item")
    private Set<OrderItemRequest> items = new HashSet<>();

    /**
     * Optional coupon discount to subtract from total (already validated
     * server-side at apply-coupon)
     */
    @DecimalMin(value = "0.0", message = "Discount amount cannot be negative")
    private BigDecimal discountAmount;

    /**
     * Optional coupon code to validate at order creation.
     */
    private String couponCode;

    /**
     * Optional delivery address fields.
     */
    private String deliveryStreet;

    private String deliveryCity;

    private String deliveryPostalCode;

    private String deliveryCountry;

    private Double deliveryLat;

    private Double deliveryLng;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {

        @NotNull(message = "Product ID is required")
        @Positive(message = "Product ID must be positive")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }
}
