package com.simplestore.paymentservice.service;

import com.simplestore.paymentservice.dto.PaymentResponse;
import com.simplestore.paymentservice.entity.Payment;
import com.simplestore.paymentservice.entity.PaymentStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class PaymentProcessingService {
    
    private final Random random = new Random();
    
    /**
     * Process payment through payment gateway (simulated)
     */
    public PaymentResponse processPayment(Payment payment) {
        try {
            // Simulate processing delay
            Thread.sleep(1000 + random.nextInt(2000));
            
            // Simulate payment gateway response
            boolean isSuccessful = simulatePaymentGateway(payment);
            
            if (isSuccessful) {
                return PaymentResponse.success(payment.getTransactionId(), payment.getAmount());
            } else {
                return PaymentResponse.failure(payment.getTransactionId(), "Payment declined by bank");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PaymentResponse.failure(payment.getTransactionId(), "Payment processing interrupted");
        } catch (Exception e) {
            return PaymentResponse.failure(payment.getTransactionId(), "Payment processing failed: " + e.getMessage());
        }
    }
    
    /**
     * Simulate payment gateway processing
     * In real implementation, this would call actual payment gateway APIs
     */
    private boolean simulatePaymentGateway(Payment payment) {
        // Simulate various failure scenarios
        
        // 1. High amount transactions have higher failure rate
        if (payment.getAmount().compareTo(new BigDecimal("1000")) > 0) {
            return random.nextDouble() > 0.3; // 30% failure rate for high amounts
        }
        
        // 2. Test card numbers (for demo purposes)
        String cardNumber = payment.getCardNumber();
        if (cardNumber.endsWith("0000")) {
            return false; // Always fail for test card ending in 0000
        }
        
        if (cardNumber.endsWith("1111")) {
            return true; // Always succeed for test card ending in 1111
        }
        
        // 3. Simulate network issues (rare)
        if (random.nextDouble() < 0.05) {
            throw new RuntimeException("Network timeout");
        }
        
        // 4. Simulate bank declines (random)
        if (random.nextDouble() < 0.15) {
            return false; // 15% decline rate
        }
        
        // 5. Simulate insufficient funds for certain amounts
        if (payment.getAmount().compareTo(new BigDecimal("500")) > 0 && 
            random.nextDouble() < 0.2) {
            return false; // 20% chance of insufficient funds for amounts > $500
        }
        
        // Default: successful payment
        return true;
    }
    
    /**
     * Process refund
     */
    public PaymentResponse processRefund(Payment originalPayment, BigDecimal refundAmount) {
        try {
            // Simulate refund processing delay
            Thread.sleep(500 + random.nextInt(1000));
            
            // Simulate refund success (higher success rate than payments)
            boolean isSuccessful = random.nextDouble() > 0.05; // 95% success rate
            
            if (isSuccessful) {
                return PaymentResponse.success(
                    "REF-" + originalPayment.getTransactionId(), 
                    refundAmount
                );
            } else {
                return PaymentResponse.failure(
                    "REF-" + originalPayment.getTransactionId(), 
                    "Refund processing failed"
                );
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PaymentResponse.failure(
                "REF-" + originalPayment.getTransactionId(), 
                "Refund processing interrupted"
            );
        }
    }
    
    /**
     * Validate payment method
     */
    public boolean validatePaymentMethod(String cardNumber, String expiryDate, String cvv) {
        // Basic validation (more comprehensive validation is done in PaymentValidationService)
        return cardNumber != null && cardNumber.length() >= 13 && 
               expiryDate != null && expiryDate.matches("^(0[1-9]|1[0-2])/([0-9]{2})$") &&
               cvv != null && cvv.matches("^[0-9]{3,4}$");
    }
}
