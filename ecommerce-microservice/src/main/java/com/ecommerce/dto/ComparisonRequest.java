package com.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonRequest {

    @NotEmpty(message = "Product IDs are required for comparison")
    @Size(min = 2, max = 4, message = "You must select between 2 and 4 products to compare")
    private Set<Long> productIds = new HashSet<>();

    // Validation: no duplicates
    @AssertTrue(message = "Duplicate product IDs are not allowed")
    public boolean isNoDuplicates() {
        if (productIds == null) {
            return true;
        }
        return productIds.size() == productIds.size(); // Set automatically prevents duplicates
    }
}
