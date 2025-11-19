package com.paymentchain.transaction.application.dto;

import java.time.LocalDateTime;

/**
 * Query criteria for finding transactions.
 *
 * @author benas
 */
public record TransactionQuery(
    String iban,
    String status,
    LocalDateTime dateFrom,
    LocalDateTime dateTo,
    String type
) {}
