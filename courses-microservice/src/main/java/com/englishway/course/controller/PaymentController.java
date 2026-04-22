package com.englishway.course.controller;

import com.englishway.course.dto.PaymentCheckoutRequest;
import com.englishway.course.dto.PaymentCheckoutResponse;
import com.englishway.course.service.PaymentService;
import com.englishway.course.service.EnrollmentService;
import com.englishway.course.util.RequestContext;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final EnrollmentService enrollmentService;

    @Value("${app.stripe.webhook.secret}")
    private String endpointSecret;

    public PaymentController(PaymentService paymentService, EnrollmentService enrollmentService) {
        this.paymentService = paymentService;
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/checkout")
    public PaymentCheckoutResponse checkout(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody PaymentCheckoutRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        return paymentService.checkoutCourse(context, request.getCourseId());
    }

    @PostMapping("/confirm/{sessionId}")
    public ResponseEntity<String> confirm(
        @PathVariable("sessionId") String sessionId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        paymentService.confirmCheckoutSession(sessionId, context);
        return ResponseEntity.ok("Payment confirmed and enrollment activated");
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getConfig() {
        return ResponseEntity.ok(Map.of("publicKey", paymentService.getPublicKey()));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
        @RequestBody String payload,
        @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            log.error("Unable to deserialize stripe object");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Deserialization failed");
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
            String userId = paymentIntent.getMetadata().get("userId");
            String courseIdStr = paymentIntent.getMetadata().get("courseId");

            if (userId != null && courseIdStr != null) {
                log.info("Payment succeeded for user {} and course {}", userId, courseIdStr);
                enrollmentService.activateEnrollmentAfterPayment(userId, UUID.fromString(courseIdStr));
            }
        }

        return ResponseEntity.ok("Success");
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(
        @PathVariable("paymentId") String paymentId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        return paymentService.getPaymentStatus(paymentId, context);
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity<String> cancelPayment(
        @PathVariable("paymentId") String paymentId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        paymentService.cancelPayment(paymentId, context);
        return ResponseEntity.ok("Payment cancelled successfully");
    }
}
