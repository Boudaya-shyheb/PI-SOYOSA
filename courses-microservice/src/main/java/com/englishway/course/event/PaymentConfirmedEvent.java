package com.englishway.course.event;

import java.util.UUID;

public class PaymentConfirmedEvent {
    private String paymentId;
    private String userId;
    private UUID courseId;

    public PaymentConfirmedEvent() {
    }

    public PaymentConfirmedEvent(String paymentId, String userId, UUID courseId) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.courseId = courseId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }
}
