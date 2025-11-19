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
 * Tests for IbanValidator.
 *
 * @author benas
 */
class IbanValidatorTest {

    private IbanValidator validator;

    @BeforeEach
    void setUp() {
        validator = new IbanValidator();
    }

    @Test
    void shouldValidateTransactionWithValidIban() {
        // Arrange
        Transaction transaction = createTransaction("ES1234567890123456789012");

        // Act & Assert
        assertThatNoException().isThrownBy(() -> validator.validate(transaction));
    }

    @Test
    void shouldAppliesToAllTransactions() {
        // Arrange
        Transaction transaction = createTransaction("ES1234567890123456789012");

        // Act & Assert
        assertThat(validator.appliesTo(transaction)).isTrue();
    }

    @Test
    void shouldReturnCorrectValidatorName() {
        assertThat(validator.getValidatorName()).isEqualTo("IbanValidator");
    }

    private Transaction createTransaction(String iban) {
        return Transaction.create(
            "TX001",
            IBAN.of(iban),
            Money.of(new BigDecimal("100"), Currency.EUR),
            TransactionType.PAYMENT,
            "WEB"
        );
    }
}
