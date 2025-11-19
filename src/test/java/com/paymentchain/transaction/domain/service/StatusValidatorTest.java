package com.paymentchain.transaction.domain.service;

import com.paymentchain.domain.model.valueobject.Currency;
import com.paymentchain.domain.model.valueobject.IBAN;
import com.paymentchain.domain.model.valueobject.Money;
import com.paymentchain.transaction.domain.model.Transaction;
import com.paymentchain.transaction.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for StatusValidator.
 *
 * @author benas
 */
class StatusValidatorTest {

    private StatusValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StatusValidator();
    }

    @Test
    void shouldValidateNewTransactionWithPendingStatus() {
        // Arrange
        Transaction transaction = createNewTransaction();

        // Act & Assert
        assertThatNoException().isThrownBy(() -> validator.validate(transaction));
    }

    @Test
    void shouldOnlyApplyToNewTransactions() {
        // Arrange
        Transaction newTransaction = createNewTransaction();
        Transaction existingTransaction = createNewTransaction();
        existingTransaction.setId(1L); // Simula transacción existente

        // Act & Assert
        assertThat(validator.appliesTo(newTransaction)).isTrue();
        assertThat(validator.appliesTo(existingTransaction)).isFalse();
    }

    @Test
    void shouldReturnCorrectValidatorName() {
        assertThat(validator.getValidatorName()).isEqualTo("StatusValidator");
    }

    private Transaction createNewTransaction() {
        return Transaction.create(
            "TX001",
            IBAN.of("ES1234567890123456789012"),
            Money.of(new BigDecimal("100"), Currency.EUR),
            TransactionType.PAYMENT,
            "WEB"
        );
    }
}
