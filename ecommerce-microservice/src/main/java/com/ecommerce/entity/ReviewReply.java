package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_replies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewReply extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "responder_id", nullable = false)
    private Long responderId;

    @Column(name = "responder_type", nullable = false, length = 50)
    private String responderType; // SELLER, ADMIN, CUSTOMER

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment;
}
