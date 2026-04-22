package com.englishway.course.dto;

import com.englishway.course.enums.EnrollmentStatus;
import java.time.Instant;
import java.util.UUID;

public class EnrollmentResponse {
    private UUID id;
    private UUID courseId;
    private String userId;
    private String userName;
    private EnrollmentStatus status;
    private Integer progressPercent;
    private Integer xpEarned;
    private Integer lastMilestone;
    private String completionBadge;
    private Instant enrolledAt;
    private Instant completedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }

    public Integer getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(Integer xpEarned) {
        this.xpEarned = xpEarned;
    }

    public Integer getLastMilestone() {
        return lastMilestone;
    }

    public void setLastMilestone(Integer lastMilestone) {
        this.lastMilestone = lastMilestone;
    }

    public String getCompletionBadge() {
        return completionBadge;
    }

    public void setCompletionBadge(String completionBadge) {
        this.completionBadge = completionBadge;
    }

    public Instant getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(Instant enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
