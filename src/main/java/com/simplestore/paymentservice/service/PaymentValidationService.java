package com.simplestore.paymentservice.service;

import com.simplestore.paymentservice.dto.PaymentRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Service
public class PaymentValidationService {
    
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^[0-9]{13,19}$");
    private static final Pattern EXPIRY_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/([0-9]{2})$");
    private static final Pattern CVV_PATTERN = Pattern.compile("^[0-9]{3,4}$");
    
    /**
     * Validate payment request
     */
    public void validatePaymentRequest(PaymentRequest request) {
        validateCardholderName(request.getCardholderName());
        validateCardNumber(request.getCardNumber());
        validateExpiryDate(request.getExpiryDate());
        validateCvv(request.getCvv());
        validateAmount(request.getAmount());
        validateOrderItems(request);
    }
    
    /**
     * Validate cardholder name
     */
    private void validateCardholderName(String cardholderName) {
        if (cardholderName == null || cardholderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Cardholder name is required");
        }
        
        if (cardholderName.trim().length() < 2) {
            throw new IllegalArgumentException("Cardholder name must be at least 2 characters");
        }
        
        if (!cardholderName.matches("^[a-zA-Z\\s]+$")) {
            throw new IllegalArgumentException("Cardholder name can only contain letters and spaces");
        }
    }
    
    /**
     * Validate card number using Luhn algorithm
     */
    private void validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Card number is required");
        }
        
        // Remove spaces and dashes
        String cleanCardNumber = cardNumber.replaceAll("[\\s-]", "");
        
        if (!CARD_NUMBER_PATTERN.matcher(cleanCardNumber).matches()) {
            throw new IllegalArgumentException("Card number must be 13-19 digits");
        }
        
        if (!isValidLuhn(cleanCardNumber)) {
            throw new IllegalArgumentException("Invalid card number");
        }
    }
    
    /**
     * Validate expiry date
     */
    private void validateExpiryDate(String expiryDate) {
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Expiry date is required");
        }
        
        if (!EXPIRY_PATTERN.matcher(expiryDate).matches()) {
            throw new IllegalArgumentException("Expiry date must be in MM/YY format");
        }
        
        // Check if card is expired
        try {
            String[] parts = expiryDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]) + 2000; // Convert YY to YYYY
            
            LocalDate expiry = LocalDate.of(year, month, 1).withDayOfMonth(
                LocalDate.of(year, month, 1).lengthOfMonth()
            );
            
            if (expiry.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Card has expired");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid expiry date format");
        }
    }
    
    /**
     * Validate CVV
     */
    private void validateCvv(String cvv) {
        if (cvv == null || cvv.trim().isEmpty()) {
            throw new IllegalArgumentException("CVV is required");
        }
        
        if (!CVV_PATTERN.matcher(cvv).matches()) {
            throw new IllegalArgumentException("CVV must be 3-4 digits");
        }
    }
    
    /**
     * Validate amount
     */
    private void validateAmount(java.math.BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        
        if (amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        
        if (amount.compareTo(new java.math.BigDecimal("999999.99")) > 0) {
            throw new IllegalArgumentException("Amount cannot exceed $999,999.99");
        }
    }
    
    /**
     * Validate order items
     */
    private void validateOrderItems(PaymentRequest request) {
        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("Order items are required");
        }
        
        // Validate each order item
        request.getOrderItems().forEach(item -> {
            if (item.getProductName() == null || item.getProductName().trim().isEmpty()) {
                throw new IllegalArgumentException("Product name is required for all items");
            }
            
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0 for all items");
            }
            
            if (item.getPrice() == null || item.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Price must be greater than 0 for all items");
            }
        });
        
        // Validate total amount matches order items
        java.math.BigDecimal calculatedTotal = request.getOrderItems().stream()
            .map(item -> item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity())))
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        if (calculatedTotal.compareTo(request.getAmount()) != 0) {
            throw new IllegalArgumentException("Total amount does not match order items total");
        }
    }
    
    /**
     * Luhn algorithm implementation for card number validation
     */
    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        
        // Loop through values starting from the rightmost side
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            
            sum += n;
            alternate = !alternate;
        }
        
        return (sum % 10 == 0);
    }
}
