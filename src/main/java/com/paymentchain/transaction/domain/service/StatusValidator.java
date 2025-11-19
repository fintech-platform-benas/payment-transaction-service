package com.paymentchain.transaction.domain.service;

import com.paymentchain.domain.exception.BusinessRuleException;
import com.paymentchain.transaction.domain.model.Transaction;
import com.paymentchain.transaction.domain.model.TransactionStatus;

/**
 * Validator: Status must be valid for creation.
 *
 * @author benas
 */
public class StatusValidator implements TransactionValidator {

    @Override
    public void validate(Transaction transaction) {
        TransactionStatus status = transaction.getStatus();

        // New transactions must be PENDING
        if (status != TransactionStatus.PENDING) {
            throw new BusinessRuleException(
                "INVALID_STATUS",
                "New transactions must have PENDING status, got: " + status
            );
        }
    }

    @Override
    public String getValidatorName() {
        return "StatusValidator";
    }

    @Override
    public boolean appliesTo(Transaction transaction) {
        return transaction.getId() == null; // Only for new transactions
    }
}
