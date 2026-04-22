package com.englishway.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class QuizCreateRequest {
    @NotNull(message = "Lesson ID is required")
    private UUID lessonId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Time limit is required")
    @Min(value = 1, message = "Time limit must be at least 1 minute")
    private Integer timeLimitMin;

    @NotNull(message = "Passing score is required")
    @Min(value = 0, message = "Passing score must be at least 0")
    private Integer passingScore;

    @NotNull(message = "Max attempts is required")
    @Min(value = 1, message = "Max attempts must be at least 1")
    private Integer maxAttempts = 1;

    @NotNull(message = "Cooldown is required")
    @Min(value = 0, message = "Cooldown must be at least 0")
    private Integer cooldownMin = 0;

    @NotEmpty(message = "At least one question is required")
    private List<QuizQuestionDto> questions;

    public UUID getLessonId() {
        return lessonId;
    }

    public void setLessonId(UUID lessonId) {
        this.lessonId = lessonId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getTimeLimitMin() {
        return timeLimitMin;
    }

    public void setTimeLimitMin(Integer timeLimitMin) {
        this.timeLimitMin = timeLimitMin;
    }

    public Integer getPassingScore() {
        return passingScore;
    }

    public void setPassingScore(Integer passingScore) {
        this.passingScore = passingScore;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Integer getCooldownMin() {
        return cooldownMin;
    }

    public void setCooldownMin(Integer cooldownMin) {
        this.cooldownMin = cooldownMin;
    }

    public List<QuizQuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuizQuestionDto> questions) {
        this.questions = questions;
    }
}
