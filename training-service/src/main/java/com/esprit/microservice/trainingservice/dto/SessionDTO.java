package com.esprit.microservice.trainingservice.dto;

import com.esprit.microservice.trainingservice.entities.SessionType;
import com.esprit.microservice.trainingservice.entities.Status;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class SessionDTO {
    private int id;

    @NotNull
    @FutureOrPresent
    private Date date;

    @NotBlank
    private String startTime;

    @Min(1)
    private int duration;

    private Status status;

    @Min(1)
    @Max(100)
    private int maxParticipants;

    private int availableSpots;
}
