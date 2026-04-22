package com.ecommerce.dto;

import com.ecommerce.entity.Bundle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BundleDTO {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private Bundle.Status status;
    private Set<BundleItemDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
