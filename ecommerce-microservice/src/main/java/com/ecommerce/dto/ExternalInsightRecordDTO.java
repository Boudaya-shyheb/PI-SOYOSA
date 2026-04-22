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
public class ExternalInsightRecordDTO {
    private Long id;
    private String source;
    private String sourceId;
    private String isbn;
    private String title;
    private String authors;
    private String publisher;
    private String publishedDate;
    private Integer pageCount;
    private String language;
    private String categories;
    private BigDecimal averageRating;
    private Integer ratingsCount;
    private String infoLink;
    private LocalDateTime updatedAt;

    private boolean matched;
    private Long productId;
    private String productName;
    private String productIsbn;
    private BigDecimal productPrice;
    private String productCategoryName;
    private BigDecimal productAverageRating;
    private Integer productReviewCount;
}
