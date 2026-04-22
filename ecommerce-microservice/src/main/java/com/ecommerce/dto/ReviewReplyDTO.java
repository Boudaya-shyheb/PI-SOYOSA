package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewReplyDTO {
    private Long id;

    @NotNull(message = "Review ID is required")
    private Long reviewId;

    @NotNull(message = "Responder ID is required")
    private Long responderId;

    @NotBlank(message = "Responder type is required")
    private String responderType;

    @NotBlank(message = "Reply comment cannot be blank")
    @Size(min = 5, max = 1000, message = "Comment must be between 5 and 1000 characters")
    private String comment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
