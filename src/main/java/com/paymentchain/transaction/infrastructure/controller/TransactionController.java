package com.paymentchain.transaction.infrastructure.controller;

import com.paymentchain.common.exception.ResourceNotFoundException;
import com.paymentchain.transaction.application.dto.CreateTransactionCommand;
import com.paymentchain.transaction.application.dto.TransactionQuery;
import com.paymentchain.transaction.application.dto.TransactionResponse;
import com.paymentchain.transaction.application.dto.UpdateTransactionCommand;
import com.paymentchain.transaction.application.port.in.CreateTransactionUseCase;
import com.paymentchain.transaction.application.port.in.FindTransactionUseCase;
import com.paymentchain.transaction.application.port.in.SettleTransactionUseCase;
import com.paymentchain.transaction.application.port.in.UpdateTransactionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for Transaction operations.
 *
 * Hexagonal Architecture: Infrastructure layer (Driving Adapter).
 * Delegates to Use Cases (Application layer).
 *
 * @author benas
 */
@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Transaction management API")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final CreateTransactionUseCase createTransactionUseCase;
    private final FindTransactionUseCase findTransactionUseCase;
    private final UpdateTransactionUseCase updateTransactionUseCase;
    private final SettleTransactionUseCase settleTransactionUseCase;

    public TransactionController(
            CreateTransactionUseCase createTransactionUseCase,
            FindTransactionUseCase findTransactionUseCase,
            UpdateTransactionUseCase updateTransactionUseCase,
            SettleTransactionUseCase settleTransactionUseCase) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.findTransactionUseCase = findTransactionUseCase;
        this.updateTransactionUseCase = updateTransactionUseCase;
        this.settleTransactionUseCase = settleTransactionUseCase;
    }

    // ==================== CREATE ====================

    @PostMapping
    @Operation(summary = "Create new transaction", description = "Creates a new payment transaction")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "409", description = "Transaction reference already exists")
    })
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionCommand command) {

        log.info("POST /api/transactions - Creating transaction: reference={}",
            command.reference());

        TransactionResponse response = createTransactionUseCase.createTransaction(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== READ ====================

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction found"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionResponse> getTransactionById(
            @Parameter(description = "Transaction ID") @PathVariable Long id) {

        log.info("GET /api/transactions/{} - Finding transaction", id);

        return findTransactionUseCase.findById(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Get transaction by reference")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction found"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionResponse> getTransactionByReference(
            @Parameter(description = "Transaction reference") @PathVariable String reference) {

        log.info("GET /api/transactions/reference/{} - Finding transaction", reference);

        return findTransactionUseCase.findByReference(reference)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", "reference", reference));
    }

    @GetMapping("/iban/{iban}")
    @Operation(summary = "Get transactions by IBAN")
    @ApiResponse(responseCode = "200", description = "Transactions found")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByIban(
            @Parameter(description = "Account IBAN") @PathVariable String iban) {

        log.info("GET /api/transactions/iban/{} - Finding transactions", iban);

        List<TransactionResponse> transactions = findTransactionUseCase.findByIban(iban);

        return ResponseEntity.ok(transactions);
    }

    @GetMapping
    @Operation(summary = "Search transactions by criteria")
    @ApiResponse(responseCode = "200", description = "Transactions found")
    public ResponseEntity<List<TransactionResponse>> searchTransactions(
            @Parameter(description = "Account IBAN") @RequestParam(required = false) String iban,
            @Parameter(description = "Transaction status") @RequestParam(required = false) String status,
            @Parameter(description = "From date") @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @Parameter(description = "To date") @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {

        log.info("GET /api/transactions - Searching: iban={}, status={}, dateFrom={}, dateTo={}",
            iban, status, dateFrom, dateTo);

        TransactionQuery query = new TransactionQuery(iban, status, dateFrom, dateTo, null);
        List<TransactionResponse> transactions = findTransactionUseCase.findByCriteria(query);

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all transactions")
    @ApiResponse(responseCode = "200", description = "All transactions retrieved")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {

        log.info("GET /api/transactions/all - Getting all transactions");

        List<TransactionResponse> transactions = findTransactionUseCase.findAll();

        return ResponseEntity.ok(transactions);
    }

    // ==================== UPDATE ====================

    @PutMapping("/{id}")
    @Operation(summary = "Update transaction")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction updated"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionResponse> updateTransaction(
            @Parameter(description = "Transaction ID") @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionCommand command) {

        log.info("PUT /api/transactions/{} - Updating transaction", id);

        TransactionResponse response = updateTransactionUseCase.updateTransaction(id, command);

        return ResponseEntity.ok(response);
    }

    // ==================== SETTLE/CANCEL ====================

    @PostMapping("/{id}/settle")
    @Operation(summary = "Settle transaction", description = "Mark transaction as settled")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction settled"),
        @ApiResponse(responseCode = "404", description = "Transaction not found"),
        @ApiResponse(responseCode = "400", description = "Cannot settle transaction in current status")
    })
    public ResponseEntity<TransactionResponse> settleTransaction(
            @Parameter(description = "Transaction ID") @PathVariable Long id) {

        log.info("POST /api/transactions/{}/settle - Settling transaction", id);

        TransactionResponse response = settleTransactionUseCase.settleTransaction(id);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel transaction", description = "Mark transaction as cancelled")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction cancelled"),
        @ApiResponse(responseCode = "404", description = "Transaction not found"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel transaction in current status")
    })
    public ResponseEntity<TransactionResponse> cancelTransaction(
            @Parameter(description = "Transaction ID") @PathVariable Long id,
            @Parameter(description = "Cancellation reason") @RequestParam String reason) {

        log.info("POST /api/transactions/{}/cancel - Cancelling transaction: reason={}", id, reason);

        TransactionResponse response = settleTransactionUseCase.cancelTransaction(id, reason);

        return ResponseEntity.ok(response);
    }
}
