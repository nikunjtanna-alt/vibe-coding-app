package com.simplestore.paymentservice.service;

import com.simplestore.paymentservice.dto.OrderItem;
import com.simplestore.paymentservice.dto.PaymentRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentValidationServiceTest {

    private final PaymentValidationService validationService = new PaymentValidationService();

    private PaymentRequest buildValidRequest() {
        return new PaymentRequest(
                "Jane Doe",
                "4111111111111111",
                "12/50",
                "123",
                new BigDecimal("12.34"),
                List.of(new OrderItem("Widget", 1, new BigDecimal("12.34")))
        );
    }

    @Test
    void validatePaymentRequest_validRequest_doesNotThrow() {
        PaymentRequest request = buildValidRequest();
        assertDoesNotThrow(() -> validationService.validatePaymentRequest(request));
    }

    @Test
    void validatePaymentRequest_invalidCardNumber_throws() {
        PaymentRequest request = buildValidRequest();
        request.setCardNumber("1234");
        assertThrows(IllegalArgumentException.class, () -> validationService.validatePaymentRequest(request));
    }

    @Test
    void validatePaymentRequest_expiredCard_throws() {
        PaymentRequest request = buildValidRequest();
        request.setExpiryDate("01/20");
        assertThrows(IllegalArgumentException.class, () -> validationService.validatePaymentRequest(request));
    }

    @Test
    void validatePaymentRequest_amountMismatch_throws() {
        PaymentRequest request = buildValidRequest();
        request.setAmount(new BigDecimal("10.00"));
        assertThrows(IllegalArgumentException.class, () -> validationService.validatePaymentRequest(request));
    }
}
