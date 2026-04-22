package com.englishway.course.service;

import com.englishway.course.dto.EnrollmentResponse;
import com.englishway.course.dto.PaymentCheckoutResponse;
import com.englishway.course.entity.Course;
import com.englishway.course.entity.Enrollment;
import com.englishway.course.enums.EnrollmentStatus;
import com.englishway.course.exception.BadRequestException;
import com.englishway.course.exception.NotFoundException;
import com.englishway.course.repository.CourseRepository;
import com.englishway.course.repository.EnrollmentRepository;
import com.englishway.course.repository.LearningMaterialRepository;
import com.englishway.course.util.RequestContext;
import com.stripe.Stripe;
import java.math.BigDecimal;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final CourseRepository courseRepository;
    private final LearningMaterialRepository materialRepository;
    private final EnrollmentService enrollmentService;
    private final AccessControlService accessControlService;
    private final EnrollmentRepository enrollmentRepository;

    @Value("${app.stripe.api.private-key}")
    private String stripePrivateKey;

    @Value("${app.stripe.api.public-key}")
    private String stripePublicKey;

    public PaymentService(
        CourseRepository courseRepository,
        LearningMaterialRepository materialRepository,
        EnrollmentService enrollmentService,
        AccessControlService accessControlService,
        EnrollmentRepository enrollmentRepository
    ) {
        this.courseRepository = courseRepository;
        this.materialRepository = materialRepository;
        this.enrollmentService = enrollmentService;
        this.accessControlService = accessControlService;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional
    public PaymentCheckoutResponse checkoutCourse(RequestContext context, UUID courseId) {
        accessControlService.requireStudent(context);

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found"));

        Enrollment enrollment = enrollmentRepository.findByCourseIdAndUserId(courseId, context.getUserId())
            .orElseGet(() -> {
                Enrollment newEnrollment = new Enrollment();
                newEnrollment.setCourse(course);
                newEnrollment.setUserId(context.getUserId());
                newEnrollment.setStatus(EnrollmentStatus.PENDING);
                newEnrollment.setProgressPercent(0);
                newEnrollment.setXpEarned(0);
                return enrollmentRepository.save(newEnrollment);
            });

        if (!course.isActive()) {
            throw new BadRequestException("Course is not active");
        }
        if (!course.isPaid()) {
            throw new BadRequestException("Course is free and does not require payment");
        }
        if (course.isPaid() && course.getPrice() != null && course.getPrice().doubleValue() < 0) {
            throw new BadRequestException("Course price cannot be negative");
        }

        if (stripePrivateKey == null || stripePrivateKey.trim().isEmpty() || stripePrivateKey.contains("sk_test_placeholder")) {
            throw new BadRequestException("Stripe API key is not configured. Please check application settings.");
        }

        Stripe.apiKey = stripePrivateKey;

        try {
            BigDecimal coursePrice = course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO;
            long unitAmount = (long) (coursePrice.doubleValue() * 100);
            
            if (unitAmount <= 0 && course.isPaid()) {
                 // Stripe requires at least 0.50 USD for card payments usually, but let's at least ensure it's > 0
                 unitAmount = 100L; // Default to 1.00 if somehow 0
            }

            SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:4200/course-view/" + courseId + "?payment_success=true&session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:4200/course-view/" + courseId + "?payment_canceled=true")
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("tnd")
                                .setUnitAmount(unitAmount)
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(course.getTitle())
                                        .build())
                                .build())
                        .build())
                .putMetadata("courseId", courseId.toString())
                .putMetadata("userId", context.getUserId())
                .build();

            Session session = Session.create(params);

            // To maintain compatibility with existing flow if frontend expects immediate pending enrollment:
            EnrollmentResponse enrollmentResp = enrollmentService.getEnrollment(courseId, context.getUserId());

            PaymentCheckoutResponse response = new PaymentCheckoutResponse();
            response.setPaymentId(session.getId());
            response.setCourseId(courseId);
            response.setStatus("REQUIRES_PAYMENT");
            response.setClientSecret(session.getId());
            response.setSessionUrl(session.getUrl());
            response.setPublicKey(stripePublicKey);
            response.setEnrollment(enrollmentResp);
            return response;
        } catch (StripeException e) {
            throw new BadRequestException("Stripe error: " + e.getMessage());
        }
    }

    public void confirmCheckoutSession(String sessionId, RequestContext context) {
        accessControlService.requireStudent(context);
        Stripe.apiKey = stripePrivateKey;

        try {
            Session session = Session.retrieve(sessionId);
            String status = session.getPaymentStatus();
            
            if ("paid".equals(status)) {
                String userId = session.getMetadata().get("userId");
                String courseIdStr = session.getMetadata().get("courseId");
                
                if (userId != null && courseIdStr != null) {
                    if (!context.getUserId().equals(userId)) {
                        throw new BadRequestException("You can only confirm your own payments");
                    }
                    enrollmentService.activateEnrollmentAfterPayment(userId, UUID.fromString(courseIdStr));
                }
            } else {
                throw new BadRequestException("Payment session is not paid. Current status: " + status);
            }
        } catch (StripeException e) {
            throw new BadRequestException("Failed to verify Stripe session: " + e.getMessage());
        }
    }

    public String getPublicKey() {
        return stripePublicKey;
    }

    public ResponseEntity<Map<String, Object>> getPaymentStatus(String paymentId, RequestContext context) {
        accessControlService.requireStudent(context);
        Stripe.apiKey = stripePrivateKey;
        
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("paymentId", intent.getId());
            response.put("status", intent.getStatus());
            response.put("amount", intent.getAmount());
            response.put("currency", intent.getCurrency());
            response.put("clientSecret", intent.getClientSecret());
            
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            throw new BadRequestException("Failed to retrieve payment status: " + e.getMessage());
        }
    }

    public void cancelPayment(String paymentId, RequestContext context) {
        accessControlService.requireStudent(context);
        Stripe.apiKey = stripePrivateKey;
        
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentId);
            
            // Verify the payment belongs to the current user
            String userId = intent.getMetadata().get("userId");
            if (!context.getUserId().equals(userId)) {
                throw new BadRequestException("You can only cancel your own payments");
            }
            
            // Only cancel if the payment is not already succeeded
            if (!intent.getStatus().equals("succeeded")) {
                intent.cancel();
            } else {
                throw new BadRequestException("Cannot cancel a payment that has already been processed");
            }
        } catch (StripeException e) {
            throw new BadRequestException("Failed to cancel payment: " + e.getMessage());
        }
    }
}
