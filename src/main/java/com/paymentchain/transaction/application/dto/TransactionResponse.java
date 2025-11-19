package com.paymentchain.transaction.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction response DTO.
 *
 * @author benas
 */
public record TransactionResponse(
    Long id,
    String reference,
    String accountIban,
    BigDecimal amount,
    String currency,
    BigDecimal fee,
    BigDecimal totalAmount,
    String description,
    String status,
    String type,
    String channel,
    boolean highValue,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime date,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt,

    CustomerInfo customer
) {

    /**
     * Factory method from domain model.
     */
    public static TransactionResponse from(
            com.paymentchain.transaction.domain.model.Transaction transaction,
            CustomerInfo customer) {

        return new TransactionResponse(
            transaction.getId(),
            transaction.getReference(),
            transaction.getAccountIban().getValue(),
            transaction.getAmount().getAmount(),
            transaction.getAmount().getCurrency().name(),
            transaction.getFee().getAmount(),
            transaction.getTotalAmount().getAmount(),
            transaction.getDescription(),
            transaction.getStatus().name(),
            transaction.getType().name(),
            transaction.getChannel(),
            transaction.isHighValue(),
            transaction.getDate(),
            transaction.getCreatedAt(),
            transaction.getUpdatedAt(),
            customer
        );
    }

    /**
     * Factory method without customer info.
     */
    public static TransactionResponse from(
            com.paymentchain.transaction.domain.model.Transaction transaction) {
        return from(transaction, null);
    }
}
