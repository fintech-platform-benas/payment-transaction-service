package com.paymentchain.transaction.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void testTransactionEntity() {
        // Given
        Transaction transaction = new Transaction();
        LocalDateTime now = LocalDateTime.now();

        // When
        transaction.setId(1L);
        transaction.setReference("TXN001");
        transaction.setAccountIban("ES1234567890");
        transaction.setDate(now);
        transaction.setAmount(100.50);
        transaction.setFee(2.50);
        transaction.setDescription("Payment for services");
        transaction.setStatus(Status.PENDING);
        transaction.setChannel(Channel.WEB);

        // Then
        assertEquals(1L, transaction.getId());
        assertEquals("TXN001", transaction.getReference());
        assertEquals("ES1234567890", transaction.getAccountIban());
        assertEquals(now, transaction.getDate());
        assertEquals(100.50, transaction.getAmount());
        assertEquals(2.50, transaction.getFee());
        assertEquals("Payment for services", transaction.getDescription());
        assertEquals(Status.PENDING, transaction.getStatus());
        assertEquals(Channel.WEB, transaction.getChannel());
    }

    @Test
    void testTransactionWithDifferentChannels() {
        // Given
        Transaction webTxn = new Transaction();
        Transaction cajeroTxn = new Transaction();
        Transaction oficinaTxn = new Transaction();

        // When
        webTxn.setChannel(Channel.WEB);
        cajeroTxn.setChannel(Channel.CAJERO);
        oficinaTxn.setChannel(Channel.OFICINA);

        // Then
        assertEquals(Channel.WEB, webTxn.getChannel());
        assertEquals(Channel.CAJERO, cajeroTxn.getChannel());
        assertEquals(Channel.OFICINA, oficinaTxn.getChannel());
    }

    @Test
    void testTransactionWithDifferentStatuses() {
        // Given
        Transaction pending = new Transaction();
        Transaction settled = new Transaction();
        Transaction rejected = new Transaction();
        Transaction cancelled = new Transaction();

        // When
        pending.setStatus(Status.PENDING);
        settled.setStatus(Status.SETTLED);
        rejected.setStatus(Status.REJECTED);
        cancelled.setStatus(Status.CANCELLED);

        // Then
        assertEquals(Status.PENDING, pending.getStatus());
        assertEquals(Status.SETTLED, settled.getStatus());
        assertEquals(Status.REJECTED, rejected.getStatus());
        assertEquals(Status.CANCELLED, cancelled.getStatus());
    }
}
