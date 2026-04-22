package com.ecommerce.controller;

import com.ecommerce.dto.ReviewDTO;
import com.ecommerce.dto.ReviewReplyDTO;
import com.ecommerce.dto.ReviewAttachmentDTO;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.service.ReviewService;
import com.ecommerce.service.UserServiceFeignService;
import com.ecommerce.util.ApiResponse;
import com.ecommerce.client.UserServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/ecommerce/reviews")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Reviews", description = "Product review management APIs")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserServiceFeignService userServiceFeignService;

    @PostMapping
    @Operation(summary = "Create review", description = "Create a new review for a product")
    public ResponseEntity<ApiResponse<ReviewDTO>> createReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ReviewDTO reviewDTO) {
        log.info("POST /api/ecommerce/reviews - Create new review for product {}", reviewDTO.getProductId());
        requireCustomerAccess(reviewDTO.getCustomerId(), authHeader);
        ReviewDTO created = reviewService.createReview(reviewDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Review created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update review", description = "Update an existing review")
    public ResponseEntity<ApiResponse<ReviewDTO>> updateReview(
            @Parameter(description = "Review ID") @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ReviewDTO reviewDTO) {
        log.info("PUT /api/ecommerce/reviews/{} - Update review", id);
        requireOwnerOrAdmin(id, authHeader);
        ReviewDTO updated = reviewService.updateReview(id, reviewDTO);
        return ResponseEntity.ok(new ApiResponse<>("Review updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete review", description = "Delete a review")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @Parameter(description = "Review ID") @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("DELETE /api/ecommerce/reviews/{} - Delete review", id);
        requireOwnerOrAdmin(id, authHeader);
        reviewService.deleteReview(id);
        return ResponseEntity.ok(new ApiResponse<>("Review deleted successfully", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID", description = "Retrieve a specific review")
    public ResponseEntity<ApiResponse<ReviewDTO>> getReview(
            @Parameter(description = "Review ID") @PathVariable Long id) {
        log.info("GET /api/ecommerce/reviews/{} - Fetch review", id);
        ReviewDTO review = reviewService.getReviewById(id);
        return ResponseEntity.ok(new ApiResponse<>("Review retrieved successfully", review));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get product reviews", description = "Retrieve all reviews for a product")
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getProductReviews(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        log.info("GET /api/ecommerce/reviews/product/{} - Fetch product reviews", productId);
        List<ReviewDTO> reviews = reviewService.getProductReviews(productId);
        return ResponseEntity.ok(new ApiResponse<>("Reviews retrieved: " + reviews.size(), reviews));
    }

    @GetMapping("/product/{productId}/verified")
    @Operation(summary = "Get verified product reviews", description = "Retrieve verified purchase reviews for a product")
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getVerifiedReviews(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        log.info("GET /api/ecommerce/reviews/product/{}/verified - Fetch verified reviews", productId);
        List<ReviewDTO> reviews = reviewService.getVerifiedReviews(productId);
        return ResponseEntity.ok(new ApiResponse<>("Verified reviews retrieved: " + reviews.size(), reviews));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer reviews", description = "Retrieve all reviews by a customer")
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getCustomerReviews(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("GET /api/ecommerce/reviews/customer/{} - Fetch customer reviews", customerId);
        requireCustomerAccess(customerId, authHeader);
        List<ReviewDTO> reviews = reviewService.getCustomerReviews(customerId);
        return ResponseEntity.ok(new ApiResponse<>("Customer reviews retrieved: " + reviews.size(), reviews));
    }

    @PostMapping("/{id}/helpful")
    @Operation(summary = "Mark review as helpful", description = "Mark a review as helpful")
    public ResponseEntity<ApiResponse<Void>> markHelpful(
            @Parameter(description = "Review ID") @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("POST /api/ecommerce/reviews/{}/helpful - Mark as helpful", id);
        requireValidToken(authHeader);
        reviewService.markHelpful(id);
        return ResponseEntity.ok(new ApiResponse<>("Review marked as helpful", null));
    }

    @PostMapping("/{id}/unhelpful")
    @Operation(summary = "Mark review as unhelpful", description = "Mark a review as unhelpful")
    public ResponseEntity<ApiResponse<Void>> markUnhelpful(
            @Parameter(description = "Review ID") @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("POST /api/ecommerce/reviews/{}/unhelpful - Mark as unhelpful", id);
        requireValidToken(authHeader);
        reviewService.markUnhelpful(id);
        return ResponseEntity.ok(new ApiResponse<>("Review marked as unhelpful", null));
    }

    @PostMapping("/{id}/replies")
    @Operation(summary = "Add reply to review", description = "Add a reply to a review")
    public ResponseEntity<ApiResponse<ReviewReplyDTO>> addReply(
            @Parameter(description = "Review ID") @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ReviewReplyDTO replyDTO) {
        log.info("POST /api/ecommerce/reviews/{}/replies - Add reply", id);
        requireValidToken(authHeader);
        ReviewReplyDTO reply = reviewService.addReply(id, replyDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Reply added successfully", reply));
    }

    @PostMapping("/{id}/attachments")
    @Operation(summary = "Add attachment to review", description = "Add an image or video attachment to a review")
    public ResponseEntity<ApiResponse<ReviewAttachmentDTO>> addAttachment(
            @Parameter(description = "Review ID") @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ReviewAttachmentDTO attachmentDTO) {
        log.info("POST /api/ecommerce/reviews/{}/attachments - Add attachment", id);
        requireValidToken(authHeader);
        ReviewAttachmentDTO attachment = reviewService.addAttachment(id, attachmentDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Attachment added successfully", attachment));
    }

    private void requireValidToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new BusinessException("Authorization header required");
        }
        if (!userServiceFeignService.validateToken(authHeader)) {
            throw new BusinessException("Invalid or expired token");
        }
    }

    private void requireCustomerAccess(Long customerId, String authHeader) {
        requireValidToken(authHeader);
        String userKey = userServiceFeignService.getUserId(authHeader);
        if (userKey == null || userKey.isBlank()) {
            throw new BusinessException("Invalid user identity");
        }
        UserServiceClient.UserInfoDto info = userServiceFeignService.getUserInfo(userKey);
        String resolvedUserId = info != null ? info.getUserId() : null;
        if (resolvedUserId == null || resolvedUserId.isBlank()) {
            throw new BusinessException("Invalid user identity");
        }
        try {
            Long tokenCustomerId = Long.valueOf(resolvedUserId);
            if (!tokenCustomerId.equals(customerId)) {
                throw new BusinessException("Customer does not match token");
            }
        } catch (NumberFormatException e) {
            throw new BusinessException("Invalid customer identity");
        }
    }

    private void requireOwnerOrAdmin(Long reviewId, String authHeader) {
        requireValidToken(authHeader);
        String role = userServiceFeignService.getUserRole(authHeader);
        if (role == null || role.isBlank()) {
            throw new BusinessException("Invalid user role");
        }
        if ("ADMIN".equals(role)) {
            return;
        }
        String userKey = userServiceFeignService.getUserId(authHeader);
        if (userKey == null || userKey.isBlank()) {
            throw new BusinessException("Invalid user identity");
        }
        ReviewDTO review = reviewService.getReviewById(reviewId);
        if (review == null || review.getCustomerId() == null) {
            throw new BusinessException("Invalid review owner");
        }
        try {
            Long tokenCustomerId = Long.valueOf(userKey);
            if (!tokenCustomerId.equals(review.getCustomerId())) {
                throw new BusinessException("Review does not belong to user");
            }
        } catch (NumberFormatException e) {
            throw new BusinessException("Invalid customer identity");
        }
    }
}
