package com.paymentchain.transaction.application.dto;

import jakarta.validation.constraints.Size;

/**
 * Command to update transaction.
 *
 * @author benas
 */
public record UpdateTransactionCommand(

    @Size(max = 500, message = "Description must be max 500 characters")
    String description,

    String status
) {}
