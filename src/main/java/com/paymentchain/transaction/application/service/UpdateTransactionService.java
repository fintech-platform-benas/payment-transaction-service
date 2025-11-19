package com.paymentchain.transaction.application.service;

import com.paymentchain.common.exception.ResourceNotFoundException;
import com.paymentchain.transaction.application.dto.TransactionResponse;
import com.paymentchain.transaction.application.dto.UpdateTransactionCommand;
import com.paymentchain.transaction.application.port.in.UpdateTransactionUseCase;
import com.paymentchain.transaction.application.port.out.TransactionRepositoryPort;
import com.paymentchain.transaction.domain.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Update Transaction.
 *
 * @author benas
 */
@Service
@Transactional
public class UpdateTransactionService implements UpdateTransactionUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateTransactionService.class);

    private final TransactionRepositoryPort repository;

    public UpdateTransactionService(TransactionRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public TransactionResponse updateTransaction(Long id, UpdateTransactionCommand command) {
        log.info("Updating transaction: id={}", id);

        Transaction transaction = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        // Update fields
        if (command.description() != null) {
            transaction.setDescription(command.description());
        }

        // Save
        Transaction updated = repository.save(transaction);
        log.info("Transaction updated: id={}", updated.getId());

        return TransactionResponse.from(updated);
    }
}
