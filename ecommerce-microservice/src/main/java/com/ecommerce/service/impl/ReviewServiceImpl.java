package com.ecommerce.service.impl;

import com.ecommerce.dto.ReviewDTO;
import com.ecommerce.dto.ReviewReplyDTO;
import com.ecommerce.dto.ReviewAttachmentDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Review;
import com.ecommerce.entity.ReviewReply;
import com.ecommerce.entity.ReviewAttachment;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.repository.ReviewReplyRepository;
import com.ecommerce.repository.ReviewAttachmentRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final ReviewAttachmentRepository reviewAttachmentRepository;
    private final ProductRepository productRepository;
        private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        log.info("Creating review for product ID: {}", reviewDTO.getProductId());

        Product product = productRepository.findById(reviewDTO.getProductId())
                .orElseThrow(() -> new BusinessException("Product not found"));

        boolean verifiedPurchase = orderItemRepository
                .existsByOrderCustomerIdAndOrderStatusAndProductId(
                        reviewDTO.getCustomerId(),
                        Order.OrderStatus.DELIVERED,
                        reviewDTO.getProductId()
                );

        Review review = Review.builder()
                .product(product)
                .customerId(reviewDTO.getCustomerId())
                .rating(reviewDTO.getRating())
                .title(reviewDTO.getTitle())
                .comment(reviewDTO.getComment())
                .helpfulCount(0)
                .unhelpfulCount(0)
                .verifiedPurchase(verifiedPurchase)
                .build();

        Review savedReview = reviewRepository.save(review);
        updateProductRating(product.getId());

        log.info("Review created successfully with ID: {}", savedReview.getId());
        return mapToDTO(savedReview);
    }

    @Override
    @Transactional
    public ReviewDTO updateReview(Long id, ReviewDTO reviewDTO) {
        log.info("Updating review with ID: {}", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Review not found"));

        review.setTitle(reviewDTO.getTitle());
        review.setComment(reviewDTO.getComment());
        review.setRating(reviewDTO.getRating());

        Review updatedReview = reviewRepository.save(review);
        updateProductRating(review.getProduct().getId());

        log.info("Review updated successfully");
        return mapToDTO(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long id) {
        log.info("Deleting review with ID: {}", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Review not found"));

        Long productId = review.getProduct().getId();
        reviewRepository.delete(review);
        updateProductRating(productId);

        log.info("Review deleted successfully");
    }

    @Override
    public ReviewDTO getReviewById(Long id) {
        log.info("Fetching review with ID: {}", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Review not found"));

        return mapToDTO(review);
    }

    @Override
    public List<ReviewDTO> getProductReviews(Long productId) {
        log.info("Fetching reviews for product ID: {}", productId);

        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDTO> getVerifiedReviews(Long productId) {
        log.info("Fetching verified reviews for product ID: {}", productId);

        return reviewRepository.findByProductIdAndVerifiedPurchaseTrue(productId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDTO> getCustomerReviews(Long customerId) {
        log.info("Fetching reviews for customer ID: {}", customerId);

        return reviewRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markHelpful(Long reviewId) {
        log.info("Marking review {} as helpful", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("Review not found"));

        review.setHelpfulCount(review.getHelpfulCount() + 1);
        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void markUnhelpful(Long reviewId) {
        log.info("Marking review {} as unhelpful", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("Review not found"));

        review.setUnhelpfulCount(review.getUnhelpfulCount() + 1);
        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public ReviewReplyDTO addReply(Long reviewId, ReviewReplyDTO replyDTO) {
        log.info("Adding reply to review ID: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("Review not found"));

        ReviewReply reply = ReviewReply.builder()
                .review(review)
                .responderId(replyDTO.getResponderId())
                .responderType(replyDTO.getResponderType())
                .comment(replyDTO.getComment())
                .build();

        ReviewReply savedReply = reviewReplyRepository.save(reply);

        log.info("Reply added successfully with ID: {}", savedReply.getId());
        return mapReplyToDTO(savedReply);
    }

    @Override
    @Transactional
    public ReviewAttachmentDTO addAttachment(Long reviewId, ReviewAttachmentDTO attachmentDTO) {
        log.info("Adding attachment to review ID: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("Review not found"));

        ReviewAttachment attachment = ReviewAttachment.builder()
                .review(review)
                .attachmentUrl(attachmentDTO.getAttachmentUrl())
                .attachmentType(attachmentDTO.getAttachmentType())
                .build();

        ReviewAttachment savedAttachment = reviewAttachmentRepository.save(attachment);

        log.info("Attachment added successfully with ID: {}", savedAttachment.getId());
        return mapAttachmentToDTO(savedAttachment);
    }

    @Transactional
    private void updateProductRating(Long productId) {
        log.info("Updating rating for product ID: {}", productId);

        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Product not found"));

        if (reviews.isEmpty()) {
            product.setAverageRating(BigDecimal.ZERO);
            product.setReviewCount(0);
        } else {
            double averageRating = reviews.stream()
                    .mapToDouble(Review::getRating)
                    .average()
                    .orElse(0.0);

            product.setAverageRating(BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP));
            product.setReviewCount(reviews.size());
        }

        productRepository.save(product);
    }

    private ReviewDTO mapToDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .customerId(review.getCustomerId())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .helpfulCount(review.getHelpfulCount())
                .unhelpfulCount(review.getUnhelpfulCount())
                .verifiedPurchase(review.getVerifiedPurchase())
                .replies(review.getReplies() != null ? review.getReplies().stream().map(this::mapReplyToDTO).collect(Collectors.toSet()) : new HashSet<>())
                .attachments(review.getAttachments() != null ? review.getAttachments().stream().map(this::mapAttachmentToDTO).collect(Collectors.toSet()) : new HashSet<>())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private ReviewReplyDTO mapReplyToDTO(ReviewReply reply) {
        return ReviewReplyDTO.builder()
                .id(reply.getId())
                .reviewId(reply.getReview().getId())
                .responderId(reply.getResponderId())
                .responderType(reply.getResponderType())
                .comment(reply.getComment())
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .build();
    }

    private ReviewAttachmentDTO mapAttachmentToDTO(ReviewAttachment attachment) {
        return ReviewAttachmentDTO.builder()
                .id(attachment.getId())
                .reviewId(attachment.getReview().getId())
                .attachmentUrl(attachment.getAttachmentUrl())
                .attachmentType(attachment.getAttachmentType())
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
