package com.esprit.microservice.trainingservice.dto;

import com.esprit.microservice.trainingservice.entities.Level;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class TrainingUpdateDTO {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private Level level;

    @NotNull
    @PositiveOrZero
    private Double price;

    private String imageUrl;
    private com.esprit.microservice.trainingservice.entities.SessionType type;
    private String meetingLink;
    private String location;
    private String room;
    private Double latitude;
    private Double longitude;
}
