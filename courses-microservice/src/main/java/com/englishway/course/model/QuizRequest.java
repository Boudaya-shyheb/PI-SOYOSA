package com.englishway.course.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

public class QuizRequest {

    private UUID courseId;
    private UUID lessonId;
    private String title;
    private Integer timeLimitMin;
    private Integer passingScore;
    private Integer maxAttempts;
    private Integer cooldownMin;
    private JsonNode questions;

    // Getters and setters

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }

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

    public JsonNode getQuestions() {
        return questions;
    }

    public void setQuestions(JsonNode questions) {
        this.questions = questions;
    }
}