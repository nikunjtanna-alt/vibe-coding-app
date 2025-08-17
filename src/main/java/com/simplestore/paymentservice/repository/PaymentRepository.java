package com.simplestore.paymentservice.repository;

import com.simplestore.paymentservice.entity.Payment;
import com.simplestore.paymentservice.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Find by transaction ID
    Optional<Payment> findByTransactionId(String transactionId);
    
    // Find by status
    List<Payment> findByStatus(PaymentStatus status);
    
    // Find by cardholder name
    List<Payment> findByCardholderNameContainingIgnoreCase(String cardholderName);
    
    // Find by amount range
    List<Payment> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    // Find by date range
    List<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find successful payments
    @Query("SELECT p FROM Payment p WHERE p.status = 'COMPLETED'")
    List<Payment> findSuccessfulPayments();
    
    // Find failed payments
    @Query("SELECT p FROM Payment p WHERE p.status IN ('FAILED', 'DECLINED')")
    List<Payment> findFailedPayments();
    
    // Get total amount by status
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status")
    BigDecimal getTotalAmountByStatus(@Param("status") PaymentStatus status);
    
    // Get payment count by status
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    Long getPaymentCountByStatus(@Param("status") PaymentStatus status);
    
    // Find payments by card number (masked for security)
    @Query("SELECT p FROM Payment p WHERE p.cardNumber LIKE %:lastFourDigits")
    List<Payment> findByCardNumberEndingWith(@Param("lastFourDigits") String lastFourDigits);
    
    // Get recent payments
    @Query("SELECT p FROM Payment p ORDER BY p.createdAt DESC")
    List<Payment> findRecentPayments();
}
