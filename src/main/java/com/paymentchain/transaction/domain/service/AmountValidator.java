package com.paymentchain.transaction.domain.service;

import com.paymentchain.domain.exception.BusinessRuleException;
import com.paymentchain.domain.model.valueobject.Money;
import com.paymentchain.transaction.domain.model.Transaction;

import java.math.BigDecimal;

/**
 * Validator: Amount must be positive and within limits.
 *
 * @author benas
 */
public class AmountValidator implements TransactionValidator {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000");
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");

    @Override
    public void validate(Transaction transaction) {
        Money amount = transaction.getAmount();

        // Check minimum
        Money minMoney = Money.of(MIN_AMOUNT, amount.getCurrency());
        if (amount.isLessThan(minMoney)) {
            throw new BusinessRuleException(
                "AMOUNT_TOO_LOW",
                "Amount must be at least " + minMoney
            );
        }

        // Check maximum
        Money maxMoney = Money.of(MAX_AMOUNT, amount.getCurrency());
        if (amount.isGreaterThan(maxMoney)) {
            throw new BusinessRuleException(
                "AMOUNT_TOO_HIGH",
                "Amount cannot exceed " + maxMoney
            );
        }
    }

    @Override
    public String getValidatorName() {
        return "AmountValidator";
    }

    @Override
    public boolean appliesTo(Transaction transaction) {
        return true; // Applies to all transactions
    }
}
