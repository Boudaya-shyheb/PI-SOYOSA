package com.ecommerce.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "search_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchLog extends BaseEntity {

    @Column(nullable = false)
    private String keyword;

    @Column(name = "search_type", nullable = false)
    private String searchType;

    @Column(name = "result_count", nullable = false)
    private Integer resultCount;

    @Column(name = "has_results", nullable = false)
    private Boolean hasResults;

    @Column(name = "user_id")
    private String userId;
}
