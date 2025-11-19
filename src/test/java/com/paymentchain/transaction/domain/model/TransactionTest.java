package com.paymentchain.transaction.domain.model;

import com.paymentchain.domain.model.valueobject.Currency;
import com.paymentchain.domain.model.valueobject.IBAN;
import com.paymentchain.domain.model.valueobject.Money;
import com.paymentchain.transaction.domain.exception.TransactionDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Transaction domain model.
 *
 * Pure domain logic - NO mocks needed.
 *
 * @author benas
 */
class TransactionTest {

    @Test
    void shouldCreateTransactionWithValidData() {
        // Arrange
        String reference = "TX001";
        IBAN iban = IBAN.of("ES1234567890123456789012");
        Money amount = Money.of(new BigDecimal("100.50"), Currency.EUR);
        TransactionType type = TransactionType.PAYMENT;
        String channel = "WEB";

        // Act
        Transaction transaction = Transaction.create(reference, iban, amount, type, channel);

        // Assert
        assertThat(transaction).isNotNull();
        assertThat(transaction.getReference()).isEqualTo(reference);
        assertThat(transaction.getAccountIban()).isEqualTo(iban);
        assertThat(transaction.getAmount()).isEqualTo(amount);
        assertThat(transaction.getType()).isEqualTo(type);
        assertThat(transaction.getChannel()).isEqualTo(channel);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction.getFee()).isEqualTo(Money.zero(Currency.EUR));
        assertThat(transaction.getDate()).isNotNull();
        assertThat(transaction.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldValidateSuccessfullyWithValidData() {
        // Arrange
        Transaction transaction = createValidTransaction();

        // Act & Assert
        assertThatNoException().isThrownBy(transaction::validate);
    }

    @Test
    void shouldRejectNullReference() {
        // Arrange
        Transaction transaction = Transaction.create(
            null,
            IBAN.of("ES1234567890123456789012"),
            Money.of(new BigDecimal("100"), Currency.EUR),
            TransactionType.PAYMENT,
            "WEB"
        );

        // Act & Assert
        assertThatThrownBy(transaction::validate)
            .isInstanceOf(TransactionDomainException.class)
            .hasMessageContaining("Reference cannot be blank");
    }

    @Test
    void shouldRejectBlankReference() {
        // Arrange
        Transaction transaction = Transaction.create(
            "   ",
            IBAN.of("ES1234567890123456789012"),
            Money.of(new BigDecimal("100"), Currency.EUR),
            TransactionType.PAYMENT,
            "WEB"
        );

        // Act & Assert
        assertThatThrownBy(transaction::validate)
            .isInstanceOf(TransactionDomainException.class)
            .hasMessageContaining("Reference cannot be blank");
    }

    @Test
    void shouldRejectReferenceTooLong() {
        // Arrange
        String longReference = "X".repeat(51);
        Transaction transaction = Transaction.create(
            longReference,
            IBAN.of("ES1234567890123456789012"),
            Money.of(new BigDecimal("100"), Currency.EUR),
            TransactionType.PAYMENT,
            "WEB"
        );

        // Act & Assert
        assertThatThrownBy(transaction::validate)
            .isInstanceOf(TransactionDomainException.class)
            .hasMessageContaining("Reference too long");
    }

    @Test
    void shouldSettleTransactionSuccessfully() {
        // Arrange
        Transaction transaction = createValidTransaction();

        // Act
        transaction.settle();

        // Assert
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.SETTLED);
        assertThat(transaction.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldRejectSettleIfNotPending() {
        // Arrange
        Transaction transaction = createValidTransaction();
        transaction.settle(); // Already settled

        // Act & Assert
        assertThatThrownBy(transaction::settle)
            .isInstanceOf(TransactionDomainException.class)
            .hasMessageContaining("Cannot settle transaction in status: SETTLED");
    }

    @Test
    void shouldCancelTransactionSuccessfully() {
        // Arrange
        Transaction transaction = createValidTransaction();
        String reason = "Customer request";

        // Act
        transaction.cancel(reason);

        // Assert
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.CANCELLED);
        assertThat(transaction.getDescription()).contains("Cancelled: " + reason);
        assertThat(transaction.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldRejectCancelIfSettled() {
        // Arrange
        Transaction transaction = createValidTransaction();
        transaction.settle();

        // Act & Assert
        assertThatThrownBy(() -> transaction.cancel("test"))
            .isInstanceOf(TransactionDomainException.class)
            .hasMessageContaining("Cannot cancel settled transaction");
    }

    @Test
    void shouldAddFeeSuccessfully() {
        // Arrange
        Transaction transaction = createValidTransaction();
        Money fee = Money.of(new BigDecimal("5.00"), Currency.EUR);

        // Act
        transaction.addFee(fee);

        // Assert
        assertThat(transaction.getFee()).isEqualTo(fee);
    }

    @Test
    void shouldRejectFeeWithDifferentCurrency() {
        // Arrange
        Transaction transaction = createValidTransaction(); // EUR
        Money fee = Money.of(new BigDecimal("5.00"), Currency.USD); // Different currency

        // Act & Assert
        assertThatThrownBy(() -> transaction.addFee(fee))
            .isInstanceOf(TransactionDomainException.class)
            .hasMessageContaining("Fee currency must match");
    }

    @Test
    void shouldCalculateTotalAmountCorrectly() {
        // Arrange
        Transaction transaction = createValidTransaction();
        Money fee = Money.of(new BigDecimal("5.00"), Currency.EUR);
        transaction.addFee(fee);

        // Act
        Money total = transaction.getTotalAmount();

        // Assert
        Money expectedTotal = Money.of(new BigDecimal("105.50"), Currency.EUR);
        assertThat(total).isEqualTo(expectedTotal);
    }

    @Test
    void shouldIdentifyHighValueTransaction() {
        // Arrange
        Transaction highValue = Transaction.create(
            "TX_HIGH",
            IBAN.of("ES1234567890123456789012"),
            Money.of(new BigDecimal("1500.00"), Currency.EUR),
            TransactionType.PAYMENT,
            "WEB"
        );

        // Act & Assert
        assertThat(highValue.isHighValue()).isTrue();
    }

    @Test
    void shouldIdentifyNonHighValueTransaction() {
        // Arrange
        Transaction lowValue = createValidTransaction(); // 100.50

        // Act & Assert
        assertThat(lowValue.isHighValue()).isFalse();
    }

    @Test
    void shouldBeEqualBasedOnReference() {
        // Arrange
        Transaction tx1 = Transaction.create(
            "TX001",
            IBAN.of("ES1234567890123456789012"),
            Money.of(new BigDecimal("100"), Currency.EUR),
            TransactionType.PAYMENT,
            "WEB"
        );

        Transaction tx2 = Transaction.create(
            "TX001", // Same reference
            IBAN.of("ES9999999999999999999999"),
            Money.of(new BigDecimal("200"), Currency.EUR),
            TransactionType.REFUND,
            "APP"
        );

        // Act & Assert
        assertThat(tx1).isEqualTo(tx2);
        assertThat(tx1.hashCode()).isEqualTo(tx2.hashCode());
    }

    // Helper method
    private Transaction createValidTransaction() {
        return Transaction.create(
            "TX001",
            IBAN.of("ES1234567890123456789012"),
            Money.of(new BigDecimal("100.50"), Currency.EUR),
            TransactionType.PAYMENT,
            "WEB"
        );
    }
}
