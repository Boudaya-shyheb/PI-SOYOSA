package com.englishway.course.event;

import java.util.UUID;

public class LessonCompletedEvent {
    private UUID courseId;
    private UUID lessonId;
    private String userId;
    private Integer xpReward;

    public LessonCompletedEvent() {
    }

    public LessonCompletedEvent(UUID courseId, UUID lessonId, String userId, Integer xpReward) {
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.userId = userId;
        this.xpReward = xpReward;
    }

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getXpReward() {
        return xpReward;
    }

    public void setXpReward(Integer xpReward) {
        this.xpReward = xpReward;
    }
}
