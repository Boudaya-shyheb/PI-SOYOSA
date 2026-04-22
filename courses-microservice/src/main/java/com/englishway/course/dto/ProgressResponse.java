package com.englishway.course.dto;

import java.util.UUID;

public class ProgressResponse {
    private UUID courseId;
    private Integer progressPercent;
    private Integer completedLessons;
    private Integer totalLessons;
    private Integer milestoneReached;
    private UUID nextLessonId;
    private java.util.List<UUID> completedLessonIds;

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }

    public Integer getCompletedLessons() {
        return completedLessons;
    }

    public void setCompletedLessons(Integer completedLessons) {
        this.completedLessons = completedLessons;
    }

    public Integer getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(Integer totalLessons) {
        this.totalLessons = totalLessons;
    }

    public Integer getMilestoneReached() {
        return milestoneReached;
    }

    public void setMilestoneReached(Integer milestoneReached) {
        this.milestoneReached = milestoneReached;
    }

    public UUID getNextLessonId() {
        return nextLessonId;
    }

    public void setNextLessonId(UUID nextLessonId) {
        this.nextLessonId = nextLessonId;
    }

    public java.util.List<UUID> getCompletedLessonIds() {
        return completedLessonIds;
    }

    public void setCompletedLessonIds(java.util.List<UUID> completedLessonIds) {
        this.completedLessonIds = completedLessonIds;
    }
}
