package com.paymentchain.transaction.domain.service;

import com.paymentchain.domain.exception.BusinessRuleException;
import com.paymentchain.transaction.domain.model.Transaction;

import java.time.LocalDateTime;

/**
 * Validator: Date cannot be in the future.
 *
 * @author benas
 */
public class DateValidator implements TransactionValidator {

    @Override
    public void validate(Transaction transaction) {
        LocalDateTime date = transaction.getDate();

        if (date == null) {
            throw new BusinessRuleException(
                "DATE_REQUIRED",
                "Transaction date is required"
            );
        }

        if (date.isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException(
                "FUTURE_DATE",
                "Transaction date cannot be in the future"
            );
        }
    }

    @Override
    public String getValidatorName() {
        return "DateValidator";
    }

    @Override
    public boolean appliesTo(Transaction transaction) {
        return true; // Applies to all transactions
    }
}
