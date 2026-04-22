package com.englishway.course.entity;

import com.englishway.course.enums.EnrollmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "enrollment")
public class Enrollment {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 100)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status;

    @Column(nullable = false)
    private Integer progressPercent;

    @Column(nullable = false)
    private Integer xpEarned;

    @Column
    private Integer lastMilestone;

    @Column(nullable = false)
    private Instant enrolledAt;

    @Column
    private Instant completedAt;

    @Column(length = 50)
    private String completionBadge;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        enrolledAt = Instant.now();
        if (progressPercent == null) {
            progressPercent = 0;
        }
        if (xpEarned == null) {
            xpEarned = 0;
        }
    }

    @PreUpdate
    void onUpdate() {
        if (progressPercent == null) {
            progressPercent = 0;
        }
        if (xpEarned == null) {
            xpEarned = 0;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
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

    public Instant getEnrolledAt() {
        return enrolledAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getCompletionBadge() {
        return completionBadge;
    }

    public void setCompletionBadge(String completionBadge) {
        this.completionBadge = completionBadge;
    }
}
