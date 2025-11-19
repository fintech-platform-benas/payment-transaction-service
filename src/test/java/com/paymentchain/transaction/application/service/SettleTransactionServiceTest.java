package com.paymentchain.transaction.application.service;

import com.paymentchain.common.exception.ResourceNotFoundException;
import com.paymentchain.domain.model.valueobject.Currency;
import com.paymentchain.domain.model.valueobject.IBAN;
import com.paymentchain.domain.model.valueobject.Money;
import com.paymentchain.transaction.application.dto.TransactionResponse;
import com.paymentchain.transaction.application.port.out.EventPublisherPort;
import com.paymentchain.transaction.application.port.out.TransactionRepositoryPort;
import com.paymentchain.transaction.domain.model.Transaction;
import com.paymentchain.transaction.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for SettleTransactionService.
 *
 * @author benas
 */
@ExtendWith(MockitoExtension.class)
class SettleTransactionServiceTest {

    @Mock
    private TransactionRepositoryPort repository;

    @Mock
    private EventPublisherPort eventPublisher;

    private SettleTransactionService service;

    @BeforeEach
    void setUp() {
        service = new SettleTransactionService(repository, eventPublisher);
    }

    @Test
    void shouldSettleTransactionSuccessfully() {
        // Arrange
        Long id = 1L;
        Transaction transaction = createTransaction();
        transaction.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(transaction));
        when(repository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        TransactionResponse response = service.settleTransaction(id);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("SETTLED");

        verify(repository).findById(id);
        verify(repository).save(any(Transaction.class));
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenSettlingNonExistentTransaction() {
        // Arrange
        Long id = 999L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.settleTransaction(id))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Transaction");

        verify(repository).findById(id);
        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldCancelTransactionSuccessfully() {
        // Arrange
        Long id = 1L;
        String reason = "Customer request";
        Transaction transaction = createTransaction();
        transaction.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(transaction));
        when(repository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        TransactionResponse response = service.cancelTransaction(id, reason);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("CANCELLED");
        assertThat(response.description()).contains(reason);

        verify(repository).findById(id);
        verify(repository).save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenCancellingNonExistentTransaction() {
        // Arrange
        Long id = 999L;
        String reason = "Test";
        when(repository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.cancelTransaction(id, reason))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Transaction");

        verify(repository).findById(id);
        verify(repository, never()).save(any());
    }

    private Transaction createTransaction() {
        return Transaction.create(
            "TX001",
            IBAN.of("ES1234567890123456789012"),
            Money.of(new BigDecimal("100"), Currency.EUR),
            TransactionType.PAYMENT,
            "WEB"
        );
    }
}
