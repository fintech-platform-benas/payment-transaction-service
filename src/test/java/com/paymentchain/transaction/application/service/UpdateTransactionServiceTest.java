package com.paymentchain.transaction.application.service;

import com.paymentchain.common.exception.ResourceNotFoundException;
import com.paymentchain.domain.model.valueobject.Currency;
import com.paymentchain.domain.model.valueobject.IBAN;
import com.paymentchain.domain.model.valueobject.Money;
import com.paymentchain.transaction.application.dto.TransactionResponse;
import com.paymentchain.transaction.application.dto.UpdateTransactionCommand;
import com.paymentchain.transaction.application.port.out.TransactionRepositoryPort;
import com.paymentchain.transaction.domain.model.Transaction;
import com.paymentchain.transaction.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for UpdateTransactionService.
 *
 * @author benas
 */
@ExtendWith(MockitoExtension.class)
class UpdateTransactionServiceTest {

    @Mock
    private TransactionRepositoryPort repository;

    private UpdateTransactionService service;

    @BeforeEach
    void setUp() {
        service = new UpdateTransactionService(repository);
    }

    @Test
    void shouldUpdateTransactionSuccessfully() {
        // Arrange
        Long id = 1L;
        UpdateTransactionCommand command = new UpdateTransactionCommand("Updated description", null);

        Transaction transaction = createTransaction();
        transaction.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(transaction));
        when(repository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        TransactionResponse response = service.updateTransaction(id, command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.description()).isEqualTo("Updated description");

        verify(repository).findById(id);
        verify(repository).save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {
        // Arrange
        Long id = 999L;
        UpdateTransactionCommand command = new UpdateTransactionCommand("Test", null);

        when(repository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.updateTransaction(id, command))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Transaction");

        verify(repository).findById(id);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldNotUpdateWhenDescriptionIsNull() {
        // Arrange
        Long id = 1L;
        UpdateTransactionCommand command = new UpdateTransactionCommand(null, null);

        Transaction transaction = createTransaction();
        transaction.setId(id);
        String originalDescription = transaction.getDescription();

        when(repository.findById(id)).thenReturn(Optional.of(transaction));
        when(repository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        TransactionResponse response = service.updateTransaction(id, command);

        // Assert
        assertThat(response.description()).isEqualTo(originalDescription);
        verify(repository).save(any(Transaction.class));
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
