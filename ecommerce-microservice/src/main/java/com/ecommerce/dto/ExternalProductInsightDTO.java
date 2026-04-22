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
public class ExternalProductInsightDTO {
    private Long productId;
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
    private String thumbnailUrl;
    private String infoLink;
    private LocalDateTime updatedAt;
}
