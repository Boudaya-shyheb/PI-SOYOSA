package com.ecommerce.repository;

import com.ecommerce.entity.ReviewAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewAttachmentRepository extends JpaRepository<ReviewAttachment, Long> {
    List<ReviewAttachment> findByReviewId(Long reviewId);
}
