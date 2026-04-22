package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewAttachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "attachment_url", columnDefinition = "TEXT", nullable = false)
    private String attachmentUrl;

    @Column(name = "attachment_type", nullable = false, length = 50)
    private String attachmentType; // IMAGE, VIDEO
}
