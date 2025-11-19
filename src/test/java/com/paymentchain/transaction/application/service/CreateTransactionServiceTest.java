package com.paymentchain.transaction.application.service;

import com.paymentchain.common.exception.ResourceNotFoundException;
import com.paymentchain.transaction.application.dto.CreateTransactionCommand;
import com.paymentchain.transaction.application.dto.CustomerInfo;
import com.paymentchain.transaction.application.dto.TransactionResponse;
import com.paymentchain.transaction.application.port.out.CustomerClientPort;
import com.paymentchain.transaction.application.port.out.EventPublisherPort;
import com.paymentchain.transaction.application.port.out.TransactionRepositoryPort;
import com.paymentchain.transaction.domain.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for CreateTransactionService.
 *
 * Uses mocks for Ports (dependencies).
 *
 * @author benas
 */
@ExtendWith(MockitoExtension.class)
class CreateTransactionServiceTest {

    @Mock
    private TransactionRepositoryPort repository;

    @Mock
    private EventPublisherPort eventPublisher;

    @Mock
    private CustomerClientPort customerClient;

    @Mock
    private ValidatorFactory validatorFactory;

    private CreateTransactionService service;

    @BeforeEach
    void setUp() {
        service = new CreateTransactionService(repository, eventPublisher, customerClient, validatorFactory);

        // Setup default validator behavior (lenient for tests that fail before this is called)
        lenient().when(validatorFactory.getValidators(any())).thenReturn(java.util.Collections.emptyList());
    }

    @Test
    void shouldCreateTransactionSuccessfully() {
        // Arrange
        CreateTransactionCommand command = new CreateTransactionCommand(
            "TX001",
            "ES1234567890123456789012",
            new BigDecimal("100.50"),
            "EUR",
            "PAYMENT",
            "WEB",
            "Test transaction",
            LocalDateTime.now()
        );

        CustomerInfo customer = new CustomerInfo(1L, "John", "Doe", "john@example.com", "+34600123456", command.accountIban());

        when(repository.existsByReference(command.reference())).thenReturn(false);
        when(customerClient.getCustomerByIban(command.accountIban())).thenReturn(Optional.of(customer));
        when(repository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(1L);
            return tx;
        });

        // Act
        TransactionResponse response = service.createTransaction(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.reference()).isEqualTo(command.reference());
        assertThat(response.amount()).isEqualByComparingTo(command.amount());
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.customer()).isEqualTo(customer);

        // Verify interactions
        verify(repository).existsByReference(command.reference());
        verify(customerClient).getCustomerByIban(command.accountIban());
        verify(repository).save(any(Transaction.class));
        verify(eventPublisher).publishToTopic(eq("transaction.created"), eq(command.accountIban()), any());
    }

    @Test
    void shouldRejectDuplicateReference() {
        // Arrange
        CreateTransactionCommand command = new CreateTransactionCommand(
            "TX001",
            "ES1234567890123456789012",
            new BigDecimal("100"),
            "EUR",
            "PAYMENT",
            "WEB",
            null,
            null
        );

        when(repository.existsByReference(command.reference())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> service.createTransaction(command))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("already exists");

        verify(repository).existsByReference(command.reference());
        verify(customerClient, never()).getCustomerByIban(any());
        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publishToTopic(any(), any(), any());
    }

    @Test
    void shouldRejectNonExistentCustomer() {
        // Arrange
        CreateTransactionCommand command = new CreateTransactionCommand(
            "TX001",
            "ES1234567890123456789012",
            new BigDecimal("100"),
            "EUR",
            "PAYMENT",
            "WEB",
            null,
            null
        );

        when(repository.existsByReference(command.reference())).thenReturn(false);
        when(customerClient.getCustomerByIban(command.accountIban())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.createTransaction(command))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Customer");

        verify(customerClient).getCustomerByIban(command.accountIban());
        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publishToTopic(any(), any(), any());
    }

    @Test
    void shouldPublishEventWithCorrectData() {
        // Arrange
        CreateTransactionCommand command = new CreateTransactionCommand(
            "TX001",
            "ES1234567890123456789012",
            new BigDecimal("100"),
            "EUR",
            "PAYMENT",
            "WEB",
            null,
            null
        );

        CustomerInfo customer = new CustomerInfo(1L, "John", "Doe", "john@example.com", null, command.accountIban());

        when(repository.existsByReference(any())).thenReturn(false);
        when(customerClient.getCustomerByIban(any())).thenReturn(Optional.of(customer));
        when(repository.save(any())).thenAnswer(inv -> {
            Transaction tx = inv.getArgument(0);
            tx.setId(1L);
            return tx;
        });

        // Act
        service.createTransaction(command);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

        verify(eventPublisher).publishToTopic(topicCaptor.capture(), keyCaptor.capture(), any());

        assertThat(topicCaptor.getValue()).isEqualTo("transaction.created");
        assertThat(keyCaptor.getValue()).isEqualTo(command.accountIban());
    }
}
