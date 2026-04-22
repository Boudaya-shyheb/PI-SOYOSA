package com.esprit.microservice.trainingservice.controllers;

import com.esprit.microservice.trainingservice.entities.Training;
import com.esprit.microservice.trainingservice.repositories.TrainingRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin("*")
public class PaymentController {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Autowired
    private TrainingRepository trainingRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @PostMapping("/create-payment-intent/{trainingId}")
    public ResponseEntity<?> createPaymentIntent(@PathVariable int trainingId) {
        try {
            Training training = trainingRepository.findById(trainingId)
                    .orElseThrow(() -> new RuntimeException("Training not found limit setup"));

            // Calculate amount in cents. Stripe uses the smallest currency unit.
            long amount = (long) (training.getPrice() * 100);

            PaymentIntentCreateParams params =
                    PaymentIntentCreateParams.builder()
                            .setAmount(amount)
                            .setCurrency("usd") // assuming usd, adjust if needed
                            .setAutomaticPaymentMethods(
                                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                            .setEnabled(true)
                                            .build()
                            )
                            .build();

            PaymentIntent intent = PaymentIntent.create(params);

            Map<String, String> responseData = new HashMap<>();
            responseData.put("clientSecret", intent.getClientSecret());

            return ResponseEntity.ok(responseData);
        } catch (StripeException e) {
            return ResponseEntity.badRequest().body("Error from Stripe api: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Internal Error: " + e.getMessage());
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String paymentId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("paymentId", intent.getId());
            response.put("status", intent.getStatus());
            response.put("amount", intent.getAmount());
            response.put("currency", intent.getCurrency());
            
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            return ResponseEntity.badRequest().body("Error retrieving payment: " + e.getMessage());
        }
    }

    @GetMapping("/config")
    public ResponseEntity<?> getPaymentConfig() {
        try {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Payment configuration");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{paymentId}")
    public ResponseEntity<?> updatePayment(@PathVariable String paymentId, @RequestBody Map<String, Object> updates) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("paymentId", intent.getId());
            response.put("status", "updated");
            
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            return ResponseEntity.badRequest().body("Error updating payment: " + e.getMessage());
        }
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity<?> cancelPayment(@PathVariable String paymentId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentId);
            
            if (!intent.getStatus().equals("succeeded")) {
                intent.cancel();
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Payment cancelled successfully");
            
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            return ResponseEntity.badRequest().body("Error cancelling payment: " + e.getMessage());
        }
    }
}