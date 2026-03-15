package com.simplestore.paymentservice.service;

import com.simplestore.paymentservice.entity.Payment;
import com.simplestore.paymentservice.entity.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentProcessingServiceTest {

    private final PaymentProcessingService processingService = new PaymentProcessingService();

    private Payment buildPayment(String cardNumber, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setCardholderName("Test User");
        payment.setCardNumber(cardNumber);
        payment.setExpiryDate("12/50");
        payment.setCvv("123");
        payment.setAmount(amount);
        payment.setTransactionId("TXN-TEST");
        return payment;
    }

    @Test
    void processPayment_alwaysSucceedsFor1111Card() {
        Payment payment = buildPayment("4111111111111111", new BigDecimal("10.00"));
        var response = processingService.processPayment(payment);
        assertEquals(PaymentStatus.COMPLETED, response.getStatus());
    }

    @Test
    void processPayment_alwaysFailsFor0000Card() {
        Payment payment = buildPayment("4111111111110000", new BigDecimal("10.00"));
        var response = processingService.processPayment(payment);
        assertEquals(PaymentStatus.FAILED, response.getStatus());
    }
}
