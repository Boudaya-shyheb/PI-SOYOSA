package com.englishway.course.event;

import com.englishway.course.enums.Level;
import java.util.UUID;

public class CourseCreatedEvent {
    private UUID courseId;
    private String title;
    private Level level;
    private boolean active;

    public CourseCreatedEvent() {
    }

    public CourseCreatedEvent(UUID courseId, String title, Level level, boolean active) {
        this.courseId = courseId;
        this.title = title;
        this.level = level;
        this.active = active;
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

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
