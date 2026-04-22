package com.englishway.course.event;

import java.util.UUID;

public class CourseUpdatedEvent {
    private UUID courseId;

    public CourseUpdatedEvent() {
    }

    public CourseUpdatedEvent(UUID courseId) {
        this.courseId = courseId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }
}
