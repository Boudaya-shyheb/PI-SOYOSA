package com.englishway.course.dto;

import jakarta.validation.constraints.NotNull;

public class QuizSubmissionGradeRequest {
    @NotNull
    private Integer score;

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
