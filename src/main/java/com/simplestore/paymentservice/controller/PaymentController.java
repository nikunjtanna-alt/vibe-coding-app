package com.simplestore.paymentservice.controller;

import com.simplestore.paymentservice.dto.PaymentRequest;
import com.simplestore.paymentservice.dto.PaymentResponse;
import com.simplestore.paymentservice.entity.Payment;
import com.simplestore.paymentservice.entity.PaymentStatus;
import com.simplestore.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*") // Allow CORS for frontend integration
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * Process a payment
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        logger.info("🔵 Payment request received - Amount: ${}, Cardholder: {}, Items: {}", 
                   request.getAmount(), request.getCardholderName(), request.getOrderItems().size());
        
        try {
            PaymentResponse response = paymentService.processPayment(request);
            
            if (response.getStatus() == PaymentStatus.COMPLETED) {
                logger.info("✅ Payment successful - Transaction ID: {}, Amount: ${}", 
                           response.getTransactionId(), response.getAmount());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("⚠️ Payment failed - Status: {}, Error: {}", 
                           response.getStatus(), response.getErrorMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            logger.error("❌ Payment processing error: {}", e.getMessage(), e);
            PaymentResponse errorResponse = PaymentResponse.failure(
                "TXN-ERROR", 
                "Payment processing failed: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * Get payment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Optional<Payment> payment = paymentService.getPaymentById(id);
        
        if (payment.isPresent()) {
            return ResponseEntity.ok(payment.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get payment by transaction ID
     */
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<Payment> getPaymentByTransactionId(@PathVariable String transactionId) {
        Optional<Payment> payment = paymentService.getPaymentByTransactionId(transactionId);
        
        if (payment.isPresent()) {
            return ResponseEntity.ok(payment.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get all payments
     */
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Get payments by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Get successful payments
     */
    @GetMapping("/successful")
    public ResponseEntity<List<Payment>> getSuccessfulPayments() {
        List<Payment> payments = paymentService.getSuccessfulPayments();
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Get failed payments
     */
    @GetMapping("/failed")
    public ResponseEntity<List<Payment>> getFailedPayments() {
        List<Payment> payments = paymentService.getFailedPayments();
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Get recent payments
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Payment>> getRecentPayments() {
        List<Payment> payments = paymentService.getRecentPayments();
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Get payment statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<PaymentStats> getPaymentStats() {
        PaymentStats stats = new PaymentStats();
        
        stats.setTotalCompleted(paymentService.getPaymentCountByStatus(PaymentStatus.COMPLETED));
        stats.setTotalFailed(paymentService.getPaymentCountByStatus(PaymentStatus.FAILED) + 
                           paymentService.getPaymentCountByStatus(PaymentStatus.DECLINED));
        stats.setTotalAmount(paymentService.getTotalAmountByStatus(PaymentStatus.COMPLETED));
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.info("🏥 Health check requested");
        return ResponseEntity.ok("Payment Service is running!");
    }
    
    /**
     * Payment statistics DTO
     */
    public static class PaymentStats {
        private Long totalCompleted;
        private Long totalFailed;
        private BigDecimal totalAmount;
        
        // Getters and Setters
        public Long getTotalCompleted() {
            return totalCompleted;
        }
        
        public void setTotalCompleted(Long totalCompleted) {
            this.totalCompleted = totalCompleted;
        }
        
        public Long getTotalFailed() {
            return totalFailed;
        }
        
        public void setTotalFailed(Long totalFailed) {
            this.totalFailed = totalFailed;
        }
        
        public BigDecimal getTotalAmount() {
            return totalAmount;
        }
        
        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }
    }
}
