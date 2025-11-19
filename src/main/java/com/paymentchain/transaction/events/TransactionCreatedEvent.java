package com.paymentchain.transaction.events;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Event emitted when a new transaction is created.
 * This event is published to Kafka topic for asynchronous processing.
 *
 * @param id Transaction unique identifier
 * @param iban Account IBAN number
 * @param amount Transaction amount
 * @param timestamp Event creation timestamp
 *
 * @author benas
 */
public record TransactionCreatedEvent(
        Long id,
        String iban,
        Double amount,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp
) implements Serializable {

    /**
     * Creates a new TransactionCreatedEvent with current timestamp.
     *
     * @param id Transaction ID
     * @param iban Account IBAN
     * @param amount Transaction amount
     * @return New event instance
     */
    public static TransactionCreatedEvent of(Long id, String iban, Double amount) {
        return new TransactionCreatedEvent(id, iban, amount, LocalDateTime.now());
    }
}
