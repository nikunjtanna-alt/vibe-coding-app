package com.simplestore.paymentservice.service;

import com.simplestore.paymentservice.dto.OrderItem;
import com.simplestore.paymentservice.dto.PaymentRequest;
import com.simplestore.paymentservice.dto.PaymentResponse;
import com.simplestore.paymentservice.entity.Payment;
import com.simplestore.paymentservice.entity.PaymentStatus;
import com.simplestore.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentValidationService validationService;

    @Mock
    private PaymentProcessingService processingService;

    @InjectMocks
    private PaymentService paymentService;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

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

    @BeforeEach
    void setUp() {
        // Ensure validation does not throw in this test
        doNothing().when(validationService).validatePaymentRequest(any());

        // Reflect expected save behavior: return same entity
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void processPayment_successPath_savesAndReturnsCompleted() {
        PaymentResponse successResponse = PaymentResponse.success("TXN-123", new BigDecimal("12.34"));
        when(processingService.processPayment(any())).thenReturn(successResponse);

        PaymentRequest request = buildValidRequest();
        PaymentResponse response = paymentService.processPayment(request);

        assertEquals(PaymentStatus.COMPLETED, response.getStatus());

        verify(paymentRepository, times(2)).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getAllValues().get(1); // second save contains status/transaction update
        // Ensure transaction id set and status updated
        assertEquals(PaymentStatus.COMPLETED, saved.getStatus());
        assertEquals("TXN-123", saved.getTransactionId());
    }
}
