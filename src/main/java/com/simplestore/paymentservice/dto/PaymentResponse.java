package com.simplestore.paymentservice.dto;

import com.simplestore.paymentservice.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    
    private String transactionId;
    private PaymentStatus status;
    private String message;
    private BigDecimal amount;
    private LocalDateTime processedAt;
    private String errorMessage;
    
    // Constructors
    public PaymentResponse() {}
    
    public PaymentResponse(String transactionId, PaymentStatus status, String message, 
                          BigDecimal amount) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
        this.amount = amount;
        this.processedAt = LocalDateTime.now();
    }
    
    public PaymentResponse(String transactionId, PaymentStatus status, String message, 
                          BigDecimal amount, String errorMessage) {
        this(transactionId, status, message, amount);
        this.errorMessage = errorMessage;
    }
    
    // Static factory methods
    public static PaymentResponse success(String transactionId, BigDecimal amount) {
        return new PaymentResponse(transactionId, PaymentStatus.COMPLETED, 
                                 "Payment processed successfully", amount);
    }
    
    public static PaymentResponse failure(String transactionId, String errorMessage) {
        return new PaymentResponse(transactionId, PaymentStatus.FAILED, 
                                 "Payment processing failed", null, errorMessage);
    }
    
    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
