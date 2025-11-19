package com.paymentchain.transaction.domain.service;

import com.paymentchain.transaction.domain.model.Transaction;

/**
 * Strategy interface for transaction validation.
 *
 * @author benas
 */
public interface TransactionValidator {

    /**
     * Validate transaction.
     *
     * @param transaction Transaction to validate
     * @throws com.paymentchain.domain.exception.BusinessRuleException if validation fails
     */
    void validate(Transaction transaction);

    /**
     * Get validator name.
     */
    String getValidatorName();

    /**
     * Check if validator applies to transaction type.
     */
    boolean appliesTo(Transaction transaction);
}
