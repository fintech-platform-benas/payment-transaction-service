package com.paymentchain.transaction.infrastructure.adapter.persistence;

import com.paymentchain.domain.model.valueobject.Currency;
import com.paymentchain.domain.model.valueobject.IBAN;
import com.paymentchain.domain.model.valueobject.Money;
import com.paymentchain.transaction.domain.model.Transaction;
import com.paymentchain.transaction.domain.model.TransactionStatus;
import com.paymentchain.transaction.domain.model.TransactionType;
import com.paymentchain.transaction.infrastructure.adapter.persistence.mapper.TransactionMapperImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for JpaTransactionAdapter with H2 in-memory database.
 *
 * Note: This test uses H2 instead of TestContainers for portability.
 * For production-like testing with PostgreSQL, TestContainers should be used
 * in CI/CD environments where Docker is available.
 *
 * @author benas
 */
@DataJpaTest
@Import({JpaTransactionAdapter.class, TransactionMapperImpl.class})
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "spring.cloud.config.import-check.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
})
class JpaTransactionAdapterTest {

    @Autowired
    private JpaTransactionAdapter adapter;

    @Test
    void shouldSaveAndRetrieveTransaction() {
        // Arrange
        Transaction transaction = createTransaction("TX_TEST_001");

        // Act
        Transaction saved = adapter.save(transaction);
        Optional<Transaction> retrieved = adapter.findById(saved.getId());

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getReference()).isEqualTo("TX_TEST_001");
        assertThat(retrieved.get().getAmount().getAmount())
            .isEqualByComparingTo(new BigDecimal("100.50"));
    }

    @Test
    void shouldFindTransactionByReference() {
        // Arrange
        Transaction transaction = createTransaction("TX_TEST_002");
        adapter.save(transaction);

        // Act
        Optional<Transaction> found = adapter.findByReference("TX_TEST_002");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getReference()).isEqualTo("TX_TEST_002");
    }

    @Test
    void shouldFindTransactionsByIban() {
        // Arrange
        String iban = "ES9999999999999999999999";
        Transaction tx1 = createTransaction("TX_TEST_003", iban);
        Transaction tx2 = createTransaction("TX_TEST_004", iban);
        adapter.save(tx1);
        adapter.save(tx2);

        // Act
        List<Transaction> found = adapter.findByAccountIban(iban);

        // Assert
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Transaction::getReference)
            .containsExactlyInAnyOrder("TX_TEST_003", "TX_TEST_004");
    }

    @Test
    void shouldFindTransactionsByStatus() {
        // Arrange
        Transaction transaction = createTransaction("TX_TEST_005");
        transaction = adapter.save(transaction);
        transaction.settle();
        adapter.save(transaction);

        // Act
        List<Transaction> settledTransactions = adapter.findByStatus(TransactionStatus.SETTLED);

        // Assert
        assertThat(settledTransactions).isNotEmpty();
        assertThat(settledTransactions)
            .anyMatch(tx -> tx.getReference().equals("TX_TEST_005"));
    }

    @Test
    void shouldFindTransactionsByDateRange() {
        // Arrange
        Transaction transaction = createTransaction("TX_TEST_006");
        adapter.save(transaction);

        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to = LocalDateTime.now().plusHours(1);

        // Act
        List<Transaction> found = adapter.findByDateBetween(from, to);

        // Assert
        assertThat(found).isNotEmpty();
        assertThat(found).anyMatch(tx -> tx.getReference().equals("TX_TEST_006"));
    }

    @Test
    void shouldCheckIfReferenceExists() {
        // Arrange
        Transaction transaction = createTransaction("TX_TEST_007");
        adapter.save(transaction);

        // Act
        boolean exists = adapter.existsByReference("TX_TEST_007");
        boolean notExists = adapter.existsByReference("TX_NONEXISTENT");

        // Assert
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldDeleteTransaction() {
        // Arrange
        Transaction transaction = createTransaction("TX_TEST_008");
        Transaction saved = adapter.save(transaction);

        // Act
        adapter.deleteById(saved.getId());
        Optional<Transaction> deleted = adapter.findById(saved.getId());

        // Assert
        assertThat(deleted).isEmpty();
    }

    @Test
    void shouldMapValueObjectsCorrectly() {
        // Arrange
        Transaction transaction = createTransaction("TX_TEST_009");

        // Act
        Transaction saved = adapter.save(transaction);
        Optional<Transaction> retrieved = adapter.findById(saved.getId());

        // Assert
        assertThat(retrieved).isPresent();
        Transaction retrievedTx = retrieved.get();

        // Value Objects preserved
        assertThat(retrievedTx.getAccountIban()).isInstanceOf(IBAN.class);
        assertThat(retrievedTx.getAmount()).isInstanceOf(Money.class);
        assertThat(retrievedTx.getFee()).isInstanceOf(Money.class);

        // Values match
        assertThat(retrievedTx.getAccountIban().getValue())
            .isEqualTo("ES1234567890123456789012");
        assertThat(retrievedTx.getAmount().getCurrency()).isEqualTo(Currency.EUR);
    }

    @Test
    void shouldFindAllTransactions() {
        // Arrange
        adapter.save(createTransaction("TX_ALL_001"));
        adapter.save(createTransaction("TX_ALL_002"));
        adapter.save(createTransaction("TX_ALL_003"));

        // Act
        List<Transaction> all = adapter.findAll();

        // Assert
        assertThat(all).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void shouldUpdateTransaction() {
        // Arrange
        Transaction transaction = createTransaction("TX_UPDATE_001");
        Transaction saved = adapter.save(transaction);

        // Act
        saved.setDescription("Updated description");
        Transaction updated = adapter.save(saved);
        Optional<Transaction> retrieved = adapter.findById(updated.getId());

        // Assert
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getDescription()).isEqualTo("Updated description");
    }

    // Helper methods
    private Transaction createTransaction(String reference) {
        return createTransaction(reference, "ES1234567890123456789012");
    }

    private Transaction createTransaction(String reference, String iban) {
        return Transaction.create(
            reference,
            IBAN.of(iban),
            Money.of(new BigDecimal("100.50"), Currency.EUR),
            TransactionType.PAYMENT,
            "WEB"
        );
    }
}
