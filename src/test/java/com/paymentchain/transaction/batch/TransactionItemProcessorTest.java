package com.paymentchain.transaction.batch;

import com.paymentchain.transaction.batch.model.TransactionSummaryDto;
import com.paymentchain.transaction.batch.processor.TransactionItemProcessor;
import com.paymentchain.transaction.infrastructure.adapter.persistence.TransactionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for TransactionItemProcessor.
 *
 * @author benas
 */
class TransactionItemProcessorTest {

    private TransactionItemProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TransactionItemProcessor();
    }

    @Test
    void shouldProcessTransactionWithCorrectFeeCalculation() throws Exception {
        // Arrange
        TransactionEntity transaction = createTestTransaction("TX001", new BigDecimal("1000.00"),
                TransactionEntity.TransactionStatusEnum.PENDING);

        // Act
        TransactionSummaryDto result = processor.process(transaction);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo(transaction.getId());
        assertThat(result.getReference()).isEqualTo("TX001");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(result.getCalculatedFee()).isEqualByComparingTo(new BigDecimal("15.00")); // 1.5% of 1000
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1015.00")); // 1000 + 15
        assertThat(result.isHighValue()).isEqualTo(false); // Exactly 1000, not > 1000
    }

    @Test
    void shouldIdentifyHighValueTransaction() throws Exception {
        // Arrange
        TransactionEntity transaction = createTestTransaction("TX002", new BigDecimal("2500.00"),
                TransactionEntity.TransactionStatusEnum.PENDING);

        // Act
        TransactionSummaryDto result = processor.process(transaction);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isHighValue()).isEqualTo(true); // > 1000
        assertThat(result.getCalculatedFee()).isEqualByComparingTo(new BigDecimal("37.50")); // 1.5% of 2500
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("2537.50"));
    }

    @Test
    void shouldFilterCancelledTransaction() throws Exception {
        // Arrange
        TransactionEntity transaction = createTestTransaction("TX003", new BigDecimal("500.00"),
                TransactionEntity.TransactionStatusEnum.CANCELLED);

        // Act
        TransactionSummaryDto result = processor.process(transaction);

        // Assert - Should return null to filter out cancelled transactions
        assertThat(result).isNull();
    }

    @Test
    void shouldProcessSmallAmountTransaction() throws Exception {
        // Arrange
        TransactionEntity transaction = createTestTransaction("TX004", new BigDecimal("50.00"),
                TransactionEntity.TransactionStatusEnum.SETTLED);

        // Act
        TransactionSummaryDto result = processor.process(transaction);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCalculatedFee()).isEqualByComparingTo(new BigDecimal("0.75")); // 1.5% of 50
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("50.75"));
        assertThat(result.isHighValue()).isEqualTo(false);
    }

    @Test
    void shouldRoundFeeCorrectly() throws Exception {
        // Arrange - Amount that requires rounding
        TransactionEntity transaction = createTestTransaction("TX005", new BigDecimal("123.45"),
                TransactionEntity.TransactionStatusEnum.PENDING);

        // Act
        TransactionSummaryDto result = processor.process(transaction);

        // Assert - Fee should be rounded to 2 decimal places
        assertThat(result).isNotNull();
        assertThat(result.getCalculatedFee()).isEqualByComparingTo(new BigDecimal("1.85")); // 1.5% of 123.45 = 1.85175 -> 1.85
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("125.30")); // 123.45 + 1.85
    }

    @Test
    void shouldProcessDifferentTransactionTypes() throws Exception {
        // Arrange
        TransactionEntity refund = createTestTransaction("TX006", new BigDecimal("200.00"),
                TransactionEntity.TransactionStatusEnum.PENDING);
        refund.setType(TransactionEntity.TransactionTypeEnum.REFUND);

        // Act
        TransactionSummaryDto result = processor.process(refund);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("REFUND");
        assertThat(result.getCalculatedFee()).isEqualByComparingTo(new BigDecimal("3.00")); // Fee still calculated
    }

    @Test
    void shouldHandleEdgeCaseAtHighValueThreshold() throws Exception {
        // Arrange - Exactly at threshold
        TransactionEntity atThreshold = createTestTransaction("TX007", new BigDecimal("1000.00"),
                TransactionEntity.TransactionStatusEnum.PENDING);

        TransactionEntity aboveThreshold = createTestTransaction("TX008", new BigDecimal("1000.01"),
                TransactionEntity.TransactionStatusEnum.PENDING);

        // Act
        TransactionSummaryDto atResult = processor.process(atThreshold);
        TransactionSummaryDto aboveResult = processor.process(aboveThreshold);

        // Assert
        assertThat(atResult.isHighValue()).isEqualTo(false); // Exactly 1000 is not > 1000
        assertThat(aboveResult.isHighValue()).isEqualTo(true); // 1000.01 is > 1000
    }

    private TransactionEntity createTestTransaction(String reference, BigDecimal amount,
                                                     TransactionEntity.TransactionStatusEnum status) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setId(1L);
        transaction.setReference(reference);
        transaction.setAccountIban("ES1234567890123456789012");
        transaction.setAmount(amount);
        transaction.setCurrency("EUR");
        transaction.setType(TransactionEntity.TransactionTypeEnum.PAYMENT);
        transaction.setChannel("WEB");
        transaction.setStatus(status);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription("Test transaction " + reference);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        return transaction;
    }
}
