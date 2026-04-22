package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    
    @NotBlank(message = "Product name cannot be blank")
    @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    private String name;

    private String isbn;
    
    private String description;

    private String imageUrl;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    private BigDecimal originalPrice;

    private BigDecimal discountPercent;

    private LocalDateTime discountEndsAt;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantityAvailable;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    private String categoryName;

    private BigDecimal averageRating;

    private Integer reviewCount;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
