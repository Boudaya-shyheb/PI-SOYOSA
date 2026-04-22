package com.ecommerce.service;

import com.ecommerce.dto.ReviewDTO;
import com.ecommerce.dto.ReviewReplyDTO;
import com.ecommerce.dto.ReviewAttachmentDTO;

import java.util.List;

public interface ReviewService {
    ReviewDTO createReview(ReviewDTO reviewDTO);
    ReviewDTO updateReview(Long id, ReviewDTO reviewDTO);
    void deleteReview(Long id);
    ReviewDTO getReviewById(Long id);
    List<ReviewDTO> getProductReviews(Long productId);
    List<ReviewDTO> getVerifiedReviews(Long productId);
    List<ReviewDTO> getCustomerReviews(Long customerId);
    void markHelpful(Long reviewId);
    void markUnhelpful(Long reviewId);
    ReviewReplyDTO addReply(Long reviewId, ReviewReplyDTO replyDTO);
    ReviewAttachmentDTO addAttachment(Long reviewId, ReviewAttachmentDTO attachmentDTO);
}
