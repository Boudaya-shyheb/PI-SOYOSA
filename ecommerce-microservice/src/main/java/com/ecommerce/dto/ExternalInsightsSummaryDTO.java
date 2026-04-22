package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalInsightsSummaryDTO {
    private long productsWithInsights;
    private long totalRecords;
    private long openAlexCount;
    private long crossrefCount;
    private LocalDateTime lastUpdatedAt;
}
