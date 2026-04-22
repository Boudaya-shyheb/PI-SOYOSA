package com.esprit.microservice.trainingservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private int id;

    @Min(1)
    @Max(5)
    private int rating;

    @NotBlank
    private String comment;
    
    private int trainingId;
    private Long studentId;
    private String studentName;
    private LocalDateTime createdAt;
}
