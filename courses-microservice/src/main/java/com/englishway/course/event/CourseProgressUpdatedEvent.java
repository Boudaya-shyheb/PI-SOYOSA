package com.englishway.course.event;

import java.util.UUID;

public class CourseProgressUpdatedEvent {
    private UUID courseId;
    private String userId;
    private Integer progressPercent;
    private Integer milestoneReached;

    public CourseProgressUpdatedEvent() {
    }

    public CourseProgressUpdatedEvent(UUID courseId, String userId, Integer progressPercent, Integer milestoneReached) {
        this.courseId = courseId;
        this.userId = userId;
        this.progressPercent = progressPercent;
        this.milestoneReached = milestoneReached;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }

    public Integer getMilestoneReached() {
        return milestoneReached;
    }

    public void setMilestoneReached(Integer milestoneReached) {
        this.milestoneReached = milestoneReached;
    }
}
