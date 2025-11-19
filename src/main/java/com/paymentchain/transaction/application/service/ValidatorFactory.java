package com.paymentchain.transaction.application.service;

import com.paymentchain.transaction.domain.model.Transaction;
import com.paymentchain.transaction.domain.service.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory pattern: Selects validators based on transaction type.
 *
 * @author benas
 */
@Component
public class ValidatorFactory {

    // Validators disponibles
    private final List<TransactionValidator> allValidators;

    public ValidatorFactory() {
        this.allValidators = List.of(
            new AmountValidator(),
            new IbanValidator(),
            new StatusValidator(),
            new DateValidator()
        );
    }

    /**
     * Get validators applicable to transaction.
     *
     * Factory logic: selecciona validadores según el tipo de transacción.
     *
     * @param transaction Transaction to validate
     * @return List of applicable validators
     */
    public List<TransactionValidator> getValidators(Transaction transaction) {
        List<TransactionValidator> applicableValidators = new ArrayList<>();

        for (TransactionValidator validator : allValidators) {
            if (validator.appliesTo(transaction)) {
                applicableValidators.add(validator);
            }
        }

        return applicableValidators;
    }

    /**
     * Get all validators (for testing/debugging).
     */
    public List<TransactionValidator> getAllValidators() {
        return new ArrayList<>(allValidators);
    }

    /**
     * Get validator by name.
     */
    public TransactionValidator getValidatorByName(String name) {
        return allValidators.stream()
            .filter(v -> v.getValidatorName().equals(name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Validator not found: " + name));
    }
}
