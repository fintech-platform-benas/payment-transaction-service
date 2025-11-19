package com.paymentchain.transaction.application.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Command to create transaction.
 *
 * @author benas
 */
public record CreateTransactionCommand(

    @NotBlank(message = "Reference is required")
    @Size(max = 50, message = "Reference must be max 50 characters")
    String reference,

    @NotBlank(message = "Account IBAN is required")
    @Pattern(regexp = "ES\\d{22}", message = "Invalid Spanish IBAN format")
    String accountIban,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @DecimalMax(value = "10000.00", message = "Amount cannot exceed 10000")
    BigDecimal amount,

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "EUR|USD|GBP", message = "Invalid currency")
    String currency,

    @NotNull(message = "Transaction type is required")
    String type,

    @NotBlank(message = "Channel is required")
    String channel,

    @Size(max = 500, message = "Description must be max 500 characters")
    String description,

    LocalDateTime date
) {

    /**
     * Constructor con valores por defecto.
     */
    public CreateTransactionCommand {
        if (date == null) {
            date = LocalDateTime.now();
        }
    }
}
