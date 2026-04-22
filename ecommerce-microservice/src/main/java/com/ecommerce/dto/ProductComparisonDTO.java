package com.ecommerce.dto;

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
public class ProductComparisonDTO {

    private List<ComparisonItemDTO> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComparisonItemDTO {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer quantityAvailable;
        private BigDecimal averageRating;
        private Integer reviewCount;
        private String imageUrl;
        private CategoryDTO category;
        private Boolean inStock;
    }
}
