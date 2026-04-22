package com.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

    private String keyword;

    @DecimalMin(value = "0.0", message = "Minimum price cannot be negative")
    private BigDecimal minPrice;

    @DecimalMax(value = "999999.99", message = "Maximum price cannot exceed 999999.99")
    private BigDecimal maxPrice;

    private Long categoryId;

    @Min(value = 1, message = "Minimum rating must be at least 1")
    @Max(value = 5, message = "Maximum rating cannot exceed 5")
    private Integer minRating;

    @Min(value = 1, message = "Minimum rating must be at least 1")
    @Max(value = 5, message = "Maximum rating cannot exceed 5")
    private Integer maxRating;

    @Pattern(regexp = "^(name|price|rating|newest)$", message = "Sort must be: name, price, rating, or newest")
    private String sortBy = "name";

    @Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be: asc or desc")
    private String sortDirection = "asc";

    @Min(value = 1, message = "Number of results must be at least 1")
    @Max(value = 1000, message = "Cannot search for more than 1000 items at once")
    private Integer limit = 100;

    // Validation: minPrice <= maxPrice
    @AssertTrue(message = "Minimum price must be less than or equal to maximum price")
    public boolean isValidPriceRange() {
        if (minPrice == null || maxPrice == null) {
            return true;
        }
        return minPrice.compareTo(maxPrice) <= 0;
    }

    // Validation: minRating <= maxRating
    @AssertTrue(message = "Minimum rating must be less than or equal to maximum rating")
    public boolean isValidRatingRange() {
        if (minRating == null || maxRating == null) {
            return true;
        }
        return minRating <= maxRating;
    }
}
