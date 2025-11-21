package com.paymentchain.transaction.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentchain.common.exception.GlobalExceptionHandler;
import com.paymentchain.common.exception.ResourceNotFoundException;
import com.paymentchain.transaction.application.dto.CreateTransactionCommand;
import com.paymentchain.transaction.application.dto.CustomerInfo;
import com.paymentchain.transaction.application.dto.TransactionQuery;
import com.paymentchain.transaction.application.dto.TransactionResponse;
import com.paymentchain.transaction.application.dto.UpdateTransactionCommand;
import com.paymentchain.transaction.application.port.in.CreateTransactionUseCase;
import com.paymentchain.transaction.application.port.in.FindTransactionUseCase;
import com.paymentchain.transaction.application.port.in.SettleTransactionUseCase;
import com.paymentchain.transaction.application.port.in.UpdateTransactionUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TransactionController with MockMvc.
 *
 * Uses @WebMvcTest for lightweight controller testing without
 * starting the full application context. Use cases are mocked.
 *
 * @author benas
 */
@WebMvcTest(TransactionController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "spring.cloud.config.import-check.enabled=false"
})
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateTransactionUseCase createTransactionUseCase;

    @MockBean
    private FindTransactionUseCase findTransactionUseCase;

    @MockBean
    private UpdateTransactionUseCase updateTransactionUseCase;

    @MockBean
    private SettleTransactionUseCase settleTransactionUseCase;

    // ==================== CREATE TESTS ====================

    @Test
    void shouldCreateTransaction() throws Exception {
        // Arrange
        CreateTransactionCommand command = new CreateTransactionCommand(
            "TX_TEST_001",
            "ES1234567890123456789012",
            new BigDecimal("100.00"),
            "EUR",
            "PAYMENT",
            "WEB",
            "Test transaction",
            LocalDateTime.now()
        );

        TransactionResponse response = createTransactionResponse(1L, "TX_TEST_001");

        when(createTransactionUseCase.createTransaction(any(CreateTransactionCommand.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.reference", is("TX_TEST_001")))
            .andExpect(jsonPath("$.accountIban", is("ES1234567890123456789012")))
            .andExpect(jsonPath("$.amount", is(100.00)))
            .andExpect(jsonPath("$.currency", is("EUR")))
            .andExpect(jsonPath("$.status", is("PENDING")));

        verify(createTransactionUseCase, times(1)).createTransaction(any(CreateTransactionCommand.class));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingTransactionWithInvalidData() throws Exception {
        // Arrange - Invalid IBAN format
        CreateTransactionCommand command = new CreateTransactionCommand(
            "TX_TEST_002",
            "INVALID_IBAN",
            new BigDecimal("100.00"),
            "EUR",
            "PAYMENT",
            "WEB",
            null,
            LocalDateTime.now()
        );

        // Act & Assert
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
            .andExpect(status().isBadRequest());

        verify(createTransactionUseCase, never()).createTransaction(any());
    }

    // ==================== READ TESTS ====================

    @Test
    void shouldGetTransactionById() throws Exception {
        // Arrange
        TransactionResponse response = createTransactionResponse(1L, "TX_TEST_003");

        when(findTransactionUseCase.findById(1L))
            .thenReturn(Optional.of(response));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.reference", is("TX_TEST_003")));

        verify(findTransactionUseCase, times(1)).findById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenTransactionDoesNotExist() throws Exception {
        // Arrange
        when(findTransactionUseCase.findById(999L))
            .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/transactions/999"))
            .andExpect(status().isNotFound());

        verify(findTransactionUseCase, times(1)).findById(999L);
    }

    @Test
    void shouldGetTransactionByReference() throws Exception {
        // Arrange
        TransactionResponse response = createTransactionResponse(1L, "TX_TEST_004");

        when(findTransactionUseCase.findByReference("TX_TEST_004"))
            .thenReturn(Optional.of(response));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/reference/TX_TEST_004"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reference", is("TX_TEST_004")));

        verify(findTransactionUseCase, times(1)).findByReference("TX_TEST_004");
    }

    @Test
    void shouldGetTransactionsByIban() throws Exception {
        // Arrange
        String iban = "ES1234567890123456789012";
        List<TransactionResponse> transactions = List.of(
            createTransactionResponse(1L, "TX_001"),
            createTransactionResponse(2L, "TX_002")
        );

        when(findTransactionUseCase.findByIban(iban))
            .thenReturn(transactions);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/iban/" + iban))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].reference", is("TX_001")))
            .andExpect(jsonPath("$[1].reference", is("TX_002")));

        verify(findTransactionUseCase, times(1)).findByIban(iban);
    }

    @Test
    void shouldSearchTransactionsByCriteria() throws Exception {
        // Arrange
        List<TransactionResponse> transactions = List.of(
            createTransactionResponse(1L, "TX_SEARCH_001")
        );

        when(findTransactionUseCase.findByCriteria(any(TransactionQuery.class)))
            .thenReturn(transactions);

        // Act & Assert
        mockMvc.perform(get("/api/transactions")
                .param("iban", "ES1234567890123456789012")
                .param("status", "PENDING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].reference", is("TX_SEARCH_001")));

        verify(findTransactionUseCase, times(1)).findByCriteria(any(TransactionQuery.class));
    }

    @Test
    void shouldGetAllTransactions() throws Exception {
        // Arrange
        List<TransactionResponse> transactions = List.of(
            createTransactionResponse(1L, "TX_ALL_001"),
            createTransactionResponse(2L, "TX_ALL_002"),
            createTransactionResponse(3L, "TX_ALL_003")
        );

        when(findTransactionUseCase.findAll())
            .thenReturn(transactions);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)));

        verify(findTransactionUseCase, times(1)).findAll();
    }

    // ==================== UPDATE TESTS ====================

    @Test
    void shouldUpdateTransaction() throws Exception {
        // Arrange
        UpdateTransactionCommand command = new UpdateTransactionCommand(
            "Updated description",
            "PENDING"
        );

        TransactionResponse response = createTransactionResponse(1L, "TX_UPDATE_001");

        when(updateTransactionUseCase.updateTransaction(eq(1L), any(UpdateTransactionCommand.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/transactions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.reference", is("TX_UPDATE_001")));

        verify(updateTransactionUseCase, times(1))
            .updateTransaction(eq(1L), any(UpdateTransactionCommand.class));
    }

    // ==================== SETTLE/CANCEL TESTS ====================

    @Test
    void shouldSettleTransaction() throws Exception {
        // Arrange
        TransactionResponse response = createTransactionResponse(1L, "TX_SETTLE_001");
        response = new TransactionResponse(
            response.id(),
            response.reference(),
            response.accountIban(),
            response.amount(),
            response.currency(),
            response.fee(),
            response.totalAmount(),
            response.description(),
            "SETTLED",  // Changed status
            response.type(),
            response.channel(),
            response.highValue(),
            response.date(),
            response.createdAt(),
            response.updatedAt(),
            response.customer()
        );

        when(settleTransactionUseCase.settleTransaction(1L))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/transactions/1/settle"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.status", is("SETTLED")));

        verify(settleTransactionUseCase, times(1)).settleTransaction(1L);
    }

    @Test
    void shouldCancelTransaction() throws Exception {
        // Arrange
        TransactionResponse response = createTransactionResponse(1L, "TX_CANCEL_001");
        response = new TransactionResponse(
            response.id(),
            response.reference(),
            response.accountIban(),
            response.amount(),
            response.currency(),
            response.fee(),
            response.totalAmount(),
            response.description(),
            "CANCELLED",  // Changed status
            response.type(),
            response.channel(),
            response.highValue(),
            response.date(),
            response.createdAt(),
            response.updatedAt(),
            response.customer()
        );

        when(settleTransactionUseCase.cancelTransaction(eq(1L), anyString()))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/transactions/1/cancel")
                .param("reason", "Customer request"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.status", is("CANCELLED")));

        verify(settleTransactionUseCase, times(1))
            .cancelTransaction(eq(1L), eq("Customer request"));
    }

    @Test
    void shouldReturnNotFoundWhenSettlingNonExistentTransaction() throws Exception {
        // Arrange
        when(settleTransactionUseCase.settleTransaction(999L))
            .thenThrow(new ResourceNotFoundException("Transaction", "id", 999L));

        // Act & Assert
        mockMvc.perform(post("/api/transactions/999/settle"))
            .andExpect(status().isNotFound());

        verify(settleTransactionUseCase, times(1)).settleTransaction(999L);
    }

    // ==================== HELPER METHODS ====================

    private TransactionResponse createTransactionResponse(Long id, String reference) {
        CustomerInfo customer = new CustomerInfo(
            1L,
            "John",
            "Doe",
            "john.doe@example.com",
            "+34600123456",
            "ES1234567890123456789012"
        );

        return new TransactionResponse(
            id,
            reference,
            "ES1234567890123456789012",
            new BigDecimal("100.00"),
            "EUR",
            BigDecimal.ZERO,
            new BigDecimal("100.00"),
            "Test transaction",
            "PENDING",
            "PAYMENT",
            "WEB",
            false,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            customer
        );
    }
}
