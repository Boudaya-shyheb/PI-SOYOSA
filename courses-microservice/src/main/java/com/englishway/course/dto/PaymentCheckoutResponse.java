package com.englishway.course.dto;

import java.util.UUID;

public class PaymentCheckoutResponse {
    private String paymentId;
    private UUID courseId;
    private String status;
    private String clientSecret;
    private String sessionUrl;
    private String publicKey;
    private EnrollmentResponse enrollment;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getSessionUrl() {
        return sessionUrl;
    }

    public void setSessionUrl(String sessionUrl) {
        this.sessionUrl = sessionUrl;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public EnrollmentResponse getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(EnrollmentResponse enrollment) {
        this.enrollment = enrollment;
    }
}
