package com.simplestore.paymentservice.service;

import com.simplestore.paymentservice.dto.PaymentRequest;
import com.simplestore.paymentservice.dto.PaymentResponse;
import com.simplestore.paymentservice.entity.Payment;
import com.simplestore.paymentservice.entity.PaymentStatus;
import com.simplestore.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private PaymentValidationService validationService;
    
    @Autowired
    private PaymentProcessingService processingService;
    
    /**
     * Process a payment request
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            // Validate payment request
            validationService.validatePaymentRequest(request);
            
            // Create payment entity
            Payment payment = createPaymentFromRequest(request);
            
            // Save payment to database
            payment = paymentRepository.save(payment);
            
            // Process payment (simulate payment gateway)
            PaymentResponse response = processingService.processPayment(payment);
            
            // Update payment status based on response
            payment.setStatus(response.getStatus());
            payment.setTransactionId(response.getTransactionId());
            
            if (response.getStatus() == PaymentStatus.FAILED) {
                payment.setErrorMessage(response.getErrorMessage());
            }
            
            paymentRepository.save(payment);
            
            return response;
            
        } catch (Exception e) {
            // Log error and return failure response
            String transactionId = generateTransactionId();
            return PaymentResponse.failure(transactionId, e.getMessage());
        }
    }
    
    /**
     * Get payment by ID
     */
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }
    
    /**
     * Get payment by transaction ID
     */
    public Optional<Payment> getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }
    
    /**
     * Get all payments
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    /**
     * Get payments by status
     */
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }
    
    /**
     * Get successful payments
     */
    public List<Payment> getSuccessfulPayments() {
        return paymentRepository.findSuccessfulPayments();
    }
    
    /**
     * Get failed payments
     */
    public List<Payment> getFailedPayments() {
        return paymentRepository.findFailedPayments();
    }
    
    /**
     * Get recent payments
     */
    public List<Payment> getRecentPayments() {
        return paymentRepository.findRecentPayments();
    }
    
    /**
     * Get total amount by status
     */
    public BigDecimal getTotalAmountByStatus(PaymentStatus status) {
        BigDecimal total = paymentRepository.getTotalAmountByStatus(status);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * Get payment count by status
     */
    public Long getPaymentCountByStatus(PaymentStatus status) {
        return paymentRepository.getPaymentCountByStatus(status);
    }
    
    /**
     * Create payment entity from request
     */
    private Payment createPaymentFromRequest(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setCardholderName(request.getCardholderName());
        payment.setCardNumber(maskCardNumber(request.getCardNumber()));
        payment.setExpiryDate(request.getExpiryDate());
        payment.setCvv("***"); // Don't store actual CVV
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId(generateTransactionId());
        return payment;
    }
    
    /**
     * Generate unique transaction ID
     */
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + 
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Mask card number for security (show only last 4 digits)
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        return "*".repeat(cardNumber.length() - 4) + cardNumber.substring(cardNumber.length() - 4);
    }
}
