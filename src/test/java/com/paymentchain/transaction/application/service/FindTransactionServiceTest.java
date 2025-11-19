package com.paymentchain.transaction.application.service;

import com.paymentchain.domain.model.valueobject.Currency;
import com.paymentchain.domain.model.valueobject.IBAN;
import com.paymentchain.domain.model.valueobject.Money;
import com.paymentchain.transaction.application.dto.CustomerInfo;
import com.paymentchain.transaction.application.dto.TransactionQuery;
import com.paymentchain.transaction.application.dto.TransactionResponse;
import com.paymentchain.transaction.application.port.out.CustomerClientPort;
import com.paymentchain.transaction.application.port.out.TransactionRepositoryPort;
import com.paymentchain.transaction.domain.model.Transaction;
import com.paymentchain.transaction.domain.model.TransactionStatus;
import com.paymentchain.transaction.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for FindTransactionService.
 *
 * @author benas
 */
@ExtendWith(MockitoExtension.class)
class FindTransactionServiceTest {

    @Mock
    private TransactionRepositoryPort repository;

    @Mock
    private CustomerClientPort customerClient;

    private FindTransactionService service;

    @BeforeEach
    void setUp() {
        service = new FindTransactionService(repository, customerClient);
    }

    @Test
    void shouldFindTransactionById() {
        // Arrange
        Transaction transaction = createTransaction(1L, "TX001");
        CustomerInfo customer = createCustomer();

        when(repository.findById(1L)).thenReturn(Optional.of(transaction));
        when(customerClient.getCustomerByIban(anyString())).thenReturn(Optional.of(customer));

        // Act
        Optional<TransactionResponse> result = service.findById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().reference()).isEqualTo("TX001");

        verify(repository).findById(1L);
    }

    @Test
    void shouldReturnEmptyWhenTransactionNotFoundById() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<TransactionResponse> result = service.findById(999L);

        // Assert
        assertThat(result).isEmpty();
        verify(repository).findById(999L);
    }

    @Test
    void shouldFindTransactionByReference() {
        // Arrange
        Transaction transaction = createTransaction(1L, "TX001");
        CustomerInfo customer = createCustomer();

        when(repository.findByReference("TX001")).thenReturn(Optional.of(transaction));
        when(customerClient.getCustomerByIban(anyString())).thenReturn(Optional.of(customer));

        // Act
        Optional<TransactionResponse> result = service.findByReference("TX001");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().reference()).isEqualTo("TX001");

        verify(repository).findByReference("TX001");
    }

    @Test
    void shouldFindTransactionsByIban() {
        // Arrange
        String iban = "ES1234567890123456789012";
        Transaction tx1 = createTransaction(1L, "TX001");
        Transaction tx2 = createTransaction(2L, "TX002");
        CustomerInfo customer = createCustomer();

        when(repository.findByAccountIban(iban)).thenReturn(Arrays.asList(tx1, tx2));
        when(customerClient.getCustomerByIban(iban)).thenReturn(Optional.of(customer));

        // Act
        List<TransactionResponse> results = service.findByIban(iban);

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).reference()).isEqualTo("TX001");
        assertThat(results.get(1).reference()).isEqualTo("TX002");
        assertThat(results.get(0).customer()).isEqualTo(customer);

        verify(repository).findByAccountIban(iban);
        verify(customerClient).getCustomerByIban(iban);
    }

    @Test
    void shouldFindTransactionsByCriteria_ByIban() {
        // Arrange
        String iban = "ES1234567890123456789012";
        TransactionQuery query = new TransactionQuery(iban, null, null, null, null);
        Transaction tx1 = createTransaction(1L, "TX001");

        when(repository.findByAccountIban(iban)).thenReturn(Arrays.asList(tx1));
        when(customerClient.getCustomerByIban(anyString())).thenReturn(Optional.empty());

        // Act
        List<TransactionResponse> results = service.findByCriteria(query);

        // Assert
        assertThat(results).hasSize(1);
        verify(repository).findByAccountIban(iban);
    }

    @Test
    void shouldFindTransactionsByCriteria_ByStatus() {
        // Arrange
        TransactionQuery query = new TransactionQuery(null, "PENDING", null, null, null);
        Transaction tx1 = createTransaction(1L, "TX001");

        when(repository.findByStatus(TransactionStatus.PENDING)).thenReturn(Arrays.asList(tx1));
        when(customerClient.getCustomerByIban(anyString())).thenReturn(Optional.empty());

        // Act
        List<TransactionResponse> results = service.findByCriteria(query);

        // Assert
        assertThat(results).hasSize(1);
        verify(repository).findByStatus(TransactionStatus.PENDING);
    }

    @Test
    void shouldFindTransactionsByCriteria_ByDateRange() {
        // Arrange
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        LocalDateTime to = LocalDateTime.now();
        TransactionQuery query = new TransactionQuery(null, null, from, to, null);
        Transaction tx1 = createTransaction(1L, "TX001");

        when(repository.findByDateBetween(from, to)).thenReturn(Arrays.asList(tx1));
        when(customerClient.getCustomerByIban(anyString())).thenReturn(Optional.empty());

        // Act
        List<TransactionResponse> results = service.findByCriteria(query);

        // Assert
        assertThat(results).hasSize(1);
        verify(repository).findByDateBetween(from, to);
    }

    @Test
    void shouldFindAllTransactions() {
        // Arrange
        Transaction tx1 = createTransaction(1L, "TX001");
        Transaction tx2 = createTransaction(2L, "TX002");

        when(repository.findAll()).thenReturn(Arrays.asList(tx1, tx2));
        when(customerClient.getCustomerByIban(anyString())).thenReturn(Optional.empty());

        // Act
        List<TransactionResponse> results = service.findAll();

        // Assert
        assertThat(results).hasSize(2);
        verify(repository).findAll();
    }

    private Transaction createTransaction(Long id, String reference) {
        Transaction transaction = Transaction.create(
            reference,
            IBAN.of("ES1234567890123456789012"),
            Money.of(new BigDecimal("100.50"), Currency.EUR),
            TransactionType.PAYMENT,
            "WEB"
        );
        transaction.setId(id);
        return transaction;
    }

    private CustomerInfo createCustomer() {
        return new CustomerInfo(1L, "John", "Doe", "john@example.com", "+34600123456", "ES1234567890123456789012");
    }
}
