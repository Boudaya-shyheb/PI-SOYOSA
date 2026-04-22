package com.esprit.microservice.trainingservice.dto;

import com.esprit.microservice.trainingservice.entities.Level;
import lombok.Data;

@Data
public class TrainingGetDTO {

    private int id;
    private String title;
    private String description;
    private Level level;
    private Double price;
}
