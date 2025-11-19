package com.paymentchain.transaction.domain.service;

import com.paymentchain.domain.exception.BusinessRuleException;
import com.paymentchain.transaction.domain.model.Transaction;

/**
 * Validator: IBAN format validation.
 *
 * @author benas
 */
public class IbanValidator implements TransactionValidator {

    @Override
    public void validate(Transaction transaction) {
        // IBAN ya está validado en Value Object
        // Esta validación adicional podría hacer checks de existencia, etc.

        if (transaction.getAccountIban() == null) {
            throw new BusinessRuleException(
                "IBAN_REQUIRED",
                "IBAN is required for transaction"
            );
        }
    }

    @Override
    public String getValidatorName() {
        return "IbanValidator";
    }

    @Override
    public boolean appliesTo(Transaction transaction) {
        return true; // Applies to all transactions
    }
}
