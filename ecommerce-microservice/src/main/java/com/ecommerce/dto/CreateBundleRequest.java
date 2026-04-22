package com.ecommerce.dto;

import com.ecommerce.entity.Bundle;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBundleRequest {
    @NotBlank(message = "Bundle name cannot be blank")
    @Size(min = 2, max = 255, message = "Bundle name must be between 2 and 255 characters")
    private String name;
    
    private String description;
    
    private String imageUrl;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;
    
    @NotNull(message = "Status is required")
    private Bundle.Status status;
    
    @NotEmpty(message = "Bundle items are required")
    @Valid
    private List<CreateBundleItemRequest> items;
}
