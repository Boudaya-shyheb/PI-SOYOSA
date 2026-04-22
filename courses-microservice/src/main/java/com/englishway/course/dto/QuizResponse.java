package com.englishway.course.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class QuizResponse {
    private UUID id;
    private UUID lessonId;
    private UUID courseId;
    private String title;
    private List<QuizQuestionDto> questions;
    private Integer timeLimitMin;
    private Integer passingScore;
    private Integer maxAttempts;
    private Integer cooldownMin;
    private Instant createdAt;
    private Instant updatedAt;

    public QuizResponse(UUID id, UUID lessonId, UUID courseId, String title, List<QuizQuestionDto> questions, Integer timeLimitMin, Integer passingScore, Integer maxAttempts, Integer cooldownMin, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.lessonId = lessonId;
        this.courseId = courseId;
        this.title = title;
        this.questions = questions;
        this.timeLimitMin = timeLimitMin;
        this.passingScore = passingScore;
        this.maxAttempts = maxAttempts;
        this.cooldownMin = cooldownMin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getLessonId() {
        return lessonId;
    }

    public void setLessonId(UUID lessonId) {
        this.lessonId = lessonId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
