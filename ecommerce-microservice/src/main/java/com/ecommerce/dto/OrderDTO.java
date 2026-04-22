package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Long id;
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    private String customerEmail;
    
    private String customerName;
    
    private String status;
    
    @DecimalMin(value = "0", message = "Total price cannot be negative")
    private BigDecimal totalPrice;
    
    private Set<OrderItemDTO> orderItems = new HashSet<>();

    private String deliveryStreet;

    private String deliveryCity;

    private String deliveryPostalCode;

    private String deliveryCountry;

    private Double deliveryLat;

    private Double deliveryLng;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
