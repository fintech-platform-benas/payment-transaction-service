package com.paymentchain.transaction.application.service;

import com.paymentchain.domain.model.valueobject.Currency;
import com.paymentchain.domain.model.valueobject.IBAN;
import com.paymentchain.domain.model.valueobject.Money;
import com.paymentchain.transaction.domain.model.Transaction;
import com.paymentchain.transaction.domain.model.TransactionType;
import com.paymentchain.transaction.domain.service.TransactionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ValidatorFactory.
 *
 * @author benas
 */
class ValidatorFactoryTest {

    private ValidatorFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ValidatorFactory();
    }

    @Test
    void shouldReturnAllValidatorsForNewTransaction() {
        // Arrange
        Transaction transaction = createNewTransaction();

        // Act
        List<TransactionValidator> validators = factory.getValidators(transaction);

        // Assert
        assertThat(validators).isNotEmpty();
        assertThat(validators).hasSize(4); // Amount, IBAN, Status, Date
    }

    @Test
    void shouldReturnValidatorByName() {
        // Act
        TransactionValidator validator = factory.getValidatorByName("AmountValidator");

        // Assert
        assertThat(validator).isNotNull();
        assertThat(validator.getValidatorName()).isEqualTo("AmountValidator");
    }

    @Test
    void shouldThrowExceptionWhenValidatorNotFound() {
        // Act & Assert
        assertThatThrownBy(() -> factory.getValidatorByName("NonExistentValidator"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Validator not found");
    }

    @Test
    void shouldReturnAllValidatorsForInspection() {
        // Act
        List<TransactionValidator> allValidators = factory.getAllValidators();

        // Assert
        assertThat(allValidators).hasSize(4);
        assertThat(allValidators).extracting(TransactionValidator::getValidatorName)
            .containsExactlyInAnyOrder("AmountValidator", "IbanValidator", "StatusValidator", "DateValidator");
    }

    private Transaction createNewTransaction() {
        return Transaction.create(
            "TX001",
            IBAN.of("ES1234567890123456789012"),
            Money.of(new BigDecimal("100.00"), Currency.EUR),
            TransactionType.PAYMENT,
            "WEB"
        );
    }
}
