package com.paymentchain.businessdomain.transaction.repository;

import com.paymentchain.businessdomain.transaction.entities.Channel;
import com.paymentchain.businessdomain.transaction.entities.Status;
import com.paymentchain.businessdomain.transaction.entities.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void testFindById() {
        // Given
        Transaction transaction = new Transaction();
        transaction.setReference("TXN001");
        transaction.setAccountIban("ES1234567890");
        transaction.setDate(LocalDateTime.now());
        transaction.setAmount(100.00);
        transaction.setFee(2.50);
        transaction.setDescription("Test transaction");
        transaction.setStatus(Status.PENDING);
        transaction.setChannel(Channel.WEB);

        Transaction saved = entityManager.persistAndFlush(transaction);

        // When
        Optional<Transaction> found = transactionRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("TXN001", found.get().getReference());
        assertEquals("ES1234567890", found.get().getAccountIban());
    }

    @Test
    void testFindByAccountIban() {
        // Given
        String accountIban = "ES9876543210";

        Transaction txn1 = new Transaction();
        txn1.setReference("TXN002");
        txn1.setAccountIban(accountIban);
        txn1.setDate(LocalDateTime.now());
        txn1.setAmount(50.00);
        txn1.setStatus(Status.SETTLED);
        txn1.setChannel(Channel.WEB);

        Transaction txn2 = new Transaction();
        txn2.setReference("TXN003");
        txn2.setAccountIban(accountIban);
        txn2.setDate(LocalDateTime.now());
        txn2.setAmount(75.00);
        txn2.setStatus(Status.PENDING);
        txn2.setChannel(Channel.CAJERO);

        entityManager.persistAndFlush(txn1);
        entityManager.persistAndFlush(txn2);

        // When
        List<Transaction> found = transactionRepository.findByAccountIban(accountIban);

        // Then
        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(t -> t.getReference().equals("TXN002")));
        assertTrue(found.stream().anyMatch(t -> t.getReference().equals("TXN003")));
    }

    @Test
    void testFindByAccountIbanNotFound() {
        // When
        List<Transaction> found = transactionRepository.findByAccountIban("NONEXISTENT");

        // Then
        assertTrue(found.isEmpty());
    }

    @Test
    void testSaveTransaction() {
        // Given
        Transaction transaction = new Transaction();
        transaction.setReference("TXN004");
        transaction.setAccountIban("ES1122334455");
        transaction.setDate(LocalDateTime.now());
        transaction.setAmount(200.00);
        transaction.setFee(5.00);
        transaction.setDescription("New transaction");
        transaction.setStatus(Status.PENDING);
        transaction.setChannel(Channel.OFICINA);

        // When
        Transaction saved = transactionRepository.save(transaction);

        // Then
        assertNotNull(saved.getId());
        assertEquals("TXN004", saved.getReference());
    }

    @Test
    void testDeleteTransaction() {
        // Given
        Transaction transaction = new Transaction();
        transaction.setReference("TXN005");
        transaction.setAccountIban("ES5544332211");
        transaction.setDate(LocalDateTime.now());
        transaction.setAmount(150.00);
        transaction.setStatus(Status.CANCELLED);
        transaction.setChannel(Channel.WEB);

        Transaction saved = entityManager.persistAndFlush(transaction);
        Long transactionId = saved.getId();

        // When
        transactionRepository.deleteById(transactionId);
        entityManager.flush();

        // Then
        Optional<Transaction> found = transactionRepository.findById(transactionId);
        assertFalse(found.isPresent());
    }
}
