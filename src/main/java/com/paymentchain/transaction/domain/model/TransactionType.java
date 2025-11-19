package com.paymentchain.transaction.domain.model;

/**
 * Transaction type enum.
 *
 * @author benas
 */
public enum TransactionType {
    PAYMENT("Payment transaction"),
    REFUND("Refund transaction"),
    TRANSFER("Bank transfer"),
    WITHDRAWAL("Cash withdrawal"),
    DEPOSIT("Deposit");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
