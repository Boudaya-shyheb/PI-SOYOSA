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
public class ReviewAttachmentDTO {
    private Long id;

    @NotNull(message = "Review ID is required")
    private Long reviewId;

    @NotBlank(message = "Attachment URL cannot be blank")
    private String attachmentUrl;

    @NotBlank(message = "Attachment type is required")
    private String attachmentType;

    private LocalDateTime createdAt;
}
