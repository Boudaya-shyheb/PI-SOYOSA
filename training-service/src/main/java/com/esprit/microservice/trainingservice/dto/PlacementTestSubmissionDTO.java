package com.esprit.microservice.trainingservice.dto;

import lombok.Data;
import java.util.Map;

@Data
public class PlacementTestSubmissionDTO {
    // Map of questionId to selectedOptionIndex
    private Map<Long, Integer> answers;
}
