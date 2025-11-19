package com.paymentchain.transaction.domain.model;

/**
 * Transaction status enum.
 *
 * @author benas
 */
public enum TransactionStatus {
    PENDING("Pending processing"),
    SETTLED("Successfully settled"),
    FAILED("Processing failed"),
    CANCELLED("Cancelled by user");

    private final String description;

    TransactionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
