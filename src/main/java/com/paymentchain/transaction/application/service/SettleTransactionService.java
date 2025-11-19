package com.paymentchain.transaction.application.service;

import com.paymentchain.common.exception.ResourceNotFoundException;
import com.paymentchain.events.transaction.TransactionSettledEvent;
import com.paymentchain.transaction.application.dto.TransactionResponse;
import com.paymentchain.transaction.application.port.in.SettleTransactionUseCase;
import com.paymentchain.transaction.application.port.out.EventPublisherPort;
import com.paymentchain.transaction.application.port.out.TransactionRepositoryPort;
import com.paymentchain.transaction.domain.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Use Case: Settle/Cancel Transaction.
 *
 * @author benas
 */
@Service
@Transactional
public class SettleTransactionService implements SettleTransactionUseCase {

    private static final Logger log = LoggerFactory.getLogger(SettleTransactionService.class);

    private final TransactionRepositoryPort repository;
    private final EventPublisherPort eventPublisher;

    public SettleTransactionService(
            TransactionRepositoryPort repository,
            EventPublisherPort eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public TransactionResponse settleTransaction(Long id) {
        log.info("Settling transaction: id={}", id);

        Transaction transaction = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        // Settle (domain logic)
        transaction.settle();

        // Save
        Transaction settled = repository.save(transaction);
        log.info("Transaction settled: id={}", settled.getId());

        // Publish event
        TransactionSettledEvent event = TransactionSettledEvent.builder()
            .transactionId(settled.getId())
            .reference(settled.getReference())
            .amount(settled.getAmount().getAmount())
            .currency(settled.getAmount().getCurrency().name())
            .settledAt(LocalDateTime.now())
            .settlementMethod("SYSTEM")
            .build();

        eventPublisher.publish(event);

        return TransactionResponse.from(settled);
    }

    @Override
    public TransactionResponse cancelTransaction(Long id, String reason) {
        log.info("Cancelling transaction: id={}, reason={}", id, reason);

        Transaction transaction = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        // Cancel (domain logic)
        transaction.cancel(reason);

        // Save
        Transaction cancelled = repository.save(transaction);
        log.info("Transaction cancelled: id={}", cancelled.getId());

        return TransactionResponse.from(cancelled);
    }
}
