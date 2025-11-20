package com.paymentchain.transaction.batch.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for transaction summary (batch processing).
 *
 * Contains calculated fields (fees, totals).
 *
 * @author benas
 */
public class TransactionSummaryDto {

    private Long transactionId;
    private String reference;
    private String accountIban;
    private LocalDateTime date;
    private BigDecimal amount;
    private String currency;
    private BigDecimal calculatedFee;
    private BigDecimal totalAmount;
    private String status;
    private String type;
    private boolean highValue;

    // Constructor vacío
    public TransactionSummaryDto() {
    }

    // Constructor completo
    public TransactionSummaryDto(
            Long transactionId,
            String reference,
            String accountIban,
            LocalDateTime date,
            BigDecimal amount,
            String currency,
            BigDecimal calculatedFee,
            BigDecimal totalAmount,
            String status,
            String type,
            boolean highValue) {
        this.transactionId = transactionId;
        this.reference = reference;
        this.accountIban = accountIban;
        this.date = date;
        this.amount = amount;
        this.currency = currency;
        this.calculatedFee = calculatedFee;
        this.totalAmount = totalAmount;
        this.status = status;
        this.type = type;
        this.highValue = highValue;
    }

    // Getters y Setters
    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getAccountIban() {
        return accountIban;
    }

    public void setAccountIban(String accountIban) {
        this.accountIban = accountIban;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getCalculatedFee() {
        return calculatedFee;
    }

    public void setCalculatedFee(BigDecimal calculatedFee) {
        this.calculatedFee = calculatedFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isHighValue() {
        return highValue;
    }

    public void setHighValue(boolean highValue) {
        this.highValue = highValue;
    }
}
