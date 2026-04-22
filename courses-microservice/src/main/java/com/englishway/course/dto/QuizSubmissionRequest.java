package com.englishway.course.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class QuizSubmissionRequest {
    @NotNull
    private List<Object> answers;

    private Integer score;

    public List<Object> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Object> answers) {
        this.answers = answers;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
