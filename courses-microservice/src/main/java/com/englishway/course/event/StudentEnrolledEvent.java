package com.englishway.course.event;

import com.englishway.course.enums.EnrollmentStatus;
import java.util.UUID;

public class StudentEnrolledEvent {
    private UUID courseId;
    private String userId;
    private EnrollmentStatus status;

    public StudentEnrolledEvent() {
    }

    public StudentEnrolledEvent(UUID courseId, String userId, EnrollmentStatus status) {
        this.courseId = courseId;
        this.userId = userId;
        this.status = status;
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

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }
}
