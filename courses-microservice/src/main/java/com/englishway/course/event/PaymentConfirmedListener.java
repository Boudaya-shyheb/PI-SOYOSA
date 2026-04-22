package com.englishway.course.event;

import com.englishway.course.service.EnrollmentService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class PaymentConfirmedListener {
    private final EnrollmentService enrollmentService;

    public PaymentConfirmedListener(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @KafkaListener(
        topics = "${app.kafka.topics.payment-confirmed}",
        containerFactory = "paymentKafkaListenerContainerFactory",
        autoStartup = "false"
    )
    public void onPaymentConfirmed(PaymentConfirmedEvent event) {
        enrollmentService.activateEnrollmentFromPayment(event);
    }
}
