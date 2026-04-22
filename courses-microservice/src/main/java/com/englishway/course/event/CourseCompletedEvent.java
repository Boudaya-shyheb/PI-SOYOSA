package com.englishway.course.event;

import java.util.UUID;

public class CourseCompletedEvent {
    private UUID courseId;
    private String userId;
    private String completionBadge;

    public CourseCompletedEvent() {
    }

    public CourseCompletedEvent(UUID courseId, String userId, String completionBadge) {
        this.courseId = courseId;
        this.userId = userId;
        this.completionBadge = completionBadge;
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

    public String getCompletionBadge() {
        return completionBadge;
    }

    public void setCompletionBadge(String completionBadge) {
        this.completionBadge = completionBadge;
    }
}
