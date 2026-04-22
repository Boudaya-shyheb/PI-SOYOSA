package com.ecommerce.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
    name = "external_product_insights",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "source"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalProductInsight extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(name = "source_id", length = 120)
    private String sourceId;

    @Column(name = "isbn", length = 32)
    private String isbn;

    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String authors;

    @Column(length = 255)
    private String publisher;

    @Column(name = "published_date", length = 32)
    private String publishedDate;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(length = 10)
    private String language;

    @Column(columnDefinition = "TEXT")
    private String categories;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "ratings_count")
    private Integer ratingsCount;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "info_link", columnDefinition = "TEXT")
    private String infoLink;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;
}
