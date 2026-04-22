package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {
    private Long id;

    private String itemType;
    
    private Long productId;
    
    private String productName;

    private BigDecimal productPrice;

    private String productImageUrl;

    private Long bundleId;

    private String bundleName;

    private BigDecimal bundlePrice;

    private String bundleImageUrl;
    
    private Integer quantity;
    
    private BigDecimal subtotal;
    
    private LocalDateTime createdAt;
}
