package com.paymentchain.transaction.domain.service;

import com.paymentchain.domain.exception.BusinessRuleException;
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
 * Tests for AmountValidator (Strategy Pattern).
 *
 * @author benas
 */
class AmountValidatorTest {

    private AmountValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AmountValidator();
    }

    @Test
    void shouldValidateAmountWithinLimits() {
        // Arrange
        Transaction transaction = createTransaction(new BigDecimal("100.00"));

        // Act & Assert
        assertThatNoException().isThrownBy(() -> validator.validate(transaction));
    }

    @Test
    void shouldRejectAmountBelowMinimum() {
        // Arrange
        Transaction transaction = createTransaction(new BigDecimal("0.001")); // < 0.01

        // Act & Assert
        assertThatThrownBy(() -> validator.validate(transaction))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Amount must be at least");
    }

    @Test
    void shouldRejectAmountAboveMaximum() {
        // Arrange
        Transaction transaction = createTransaction(new BigDecimal("10001.00")); // > 10000

        // Act & Assert
        assertThatThrownBy(() -> validator.validate(transaction))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Amount cannot exceed");
    }

    @Test
    void shouldAcceptMinimumAmount() {
        // Arrange
        Transaction transaction = createTransaction(new BigDecimal("0.01"));

        // Act & Assert
        assertThatNoException().isThrownBy(() -> validator.validate(transaction));
    }

    @Test
    void shouldAcceptMaximumAmount() {
        // Arrange
        Transaction transaction = createTransaction(new BigDecimal("10000.00"));

        // Act & Assert
        assertThatNoException().isThrownBy(() -> validator.validate(transaction));
    }

    @Test
    void shouldAppliesToAllTransactions() {
        // Arrange
        Transaction transaction = createTransaction(new BigDecimal("100"));

        // Act & Assert
        assertThat(validator.appliesTo(transaction)).isTrue();
    }

    @Test
    void shouldReturnCorrectValidatorName() {
        assertThat(validator.getValidatorName()).isEqualTo("AmountValidator");
    }

    private Transaction createTransaction(BigDecimal amount) {
        return Transaction.create(
            "TX001",
            IBAN.of("ES1234567890123456789012"),
            Money.of(amount, Currency.EUR),
            TransactionType.PAYMENT,
            "WEB"
        );
    }
}
