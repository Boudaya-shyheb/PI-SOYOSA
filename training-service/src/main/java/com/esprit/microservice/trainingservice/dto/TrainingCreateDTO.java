package com.esprit.microservice.trainingservice.dto;

import com.esprit.microservice.trainingservice.entities.Level;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TrainingCreateDTO {
    @NotBlank
    private String title;

    @NotBlank
    private String description;


    private Level level;

    @NotNull
    @Positive
    private Double price;

    private String imageUrl;
    private com.esprit.microservice.trainingservice.entities.SessionType type;
    private String meetingLink;
    private String location;
    private String room;
    private Double latitude;
    private Double longitude;
}
