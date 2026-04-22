package com.example.gameservice.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class QuizQuestionDto extends GameContentDTO {
    private String questionText;
    private String correctAnswer;
}