package com.paymentchain.transaction.application.service;

import com.paymentchain.transaction.application.dto.CustomerInfo;
import com.paymentchain.transaction.application.dto.TransactionQuery;
import com.paymentchain.transaction.application.dto.TransactionResponse;
import com.paymentchain.transaction.application.port.in.FindTransactionUseCase;
import com.paymentchain.transaction.application.port.out.CustomerClientPort;
import com.paymentchain.transaction.application.port.out.TransactionRepositoryPort;
import com.paymentchain.transaction.domain.model.Transaction;
import com.paymentchain.transaction.domain.model.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Use Case: Find Transactions.
 *
 * @author benas
 */
@Service
@Transactional(readOnly = true)
public class FindTransactionService implements FindTransactionUseCase {

    private static final Logger log = LoggerFactory.getLogger(FindTransactionService.class);

    private final TransactionRepositoryPort repository;
    private final CustomerClientPort customerClient;

    public FindTransactionService(
            TransactionRepositoryPort repository,
            CustomerClientPort customerClient) {
        this.repository = repository;
        this.customerClient = customerClient;
    }

    @Override
    public Optional<TransactionResponse> findById(Long id) {
        log.debug("Finding transaction by id: {}", id);

        return repository.findById(id)
            .map(this::toResponse);
    }

    @Override
    public Optional<TransactionResponse> findByReference(String reference) {
        log.debug("Finding transaction by reference: {}", reference);

        return repository.findByReference(reference)
            .map(this::toResponse);
    }

    @Override
    public List<TransactionResponse> findByIban(String iban) {
        log.debug("Finding transactions by IBAN: {}", iban);

        List<Transaction> transactions = repository.findByAccountIban(iban);

        // Get customer info once
        CustomerInfo customer = customerClient.getCustomerByIban(iban).orElse(null);

        return transactions.stream()
            .map(tx -> TransactionResponse.from(tx, customer))
            .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> findByCriteria(TransactionQuery query) {
        log.debug("Finding transactions by criteria: {}", query);

        List<Transaction> transactions;

        // Apply filters (simple implementation)
        if (query.iban() != null) {
            transactions = repository.findByAccountIban(query.iban());
        } else if (query.status() != null) {
            transactions = repository.findByStatus(TransactionStatus.valueOf(query.status()));
        } else if (query.dateFrom() != null && query.dateTo() != null) {
            transactions = repository.findByDateBetween(query.dateFrom(), query.dateTo());
        } else {
            transactions = repository.findAll();
        }

        return transactions.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> findAll() {
        log.debug("Finding all transactions");

        return repository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Convert domain model to response DTO.
     */
    private TransactionResponse toResponse(Transaction transaction) {
        // Try to get customer info (optional)
        CustomerInfo customer = customerClient
            .getCustomerByIban(transaction.getAccountIban().getValue())
            .orElse(null);

        return TransactionResponse.from(transaction, customer);
    }
}
