package com.paymentchain.transaction.application.dto;

/**
 * Customer information DTO (from Customer service).
 *
 * @author benas
 */
public record CustomerInfo(
    Long id,
    String name,
    String surname,
    String email,
    String phone,
    String iban
) {}
