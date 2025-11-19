package com.paymentchain.transaction.application.port.in;

import com.paymentchain.transaction.application.dto.TransactionQuery;
import com.paymentchain.transaction.application.dto.TransactionResponse;

import java.util.List;
import java.util.Optional;

/**
 * Port IN: Find transactions.
 *
 * @author benas
 */
public interface FindTransactionUseCase {

    /**
     * Find transaction by ID.
     */
    Optional<TransactionResponse> findById(Long id);

    /**
     * Find transaction by reference.
     */
    Optional<TransactionResponse> findByReference(String reference);

    /**
     * Find transactions by IBAN.
     */
    List<TransactionResponse> findByIban(String iban);

    /**
     * Find transactions by criteria.
     */
    List<TransactionResponse> findByCriteria(TransactionQuery query);

    /**
     * Find all transactions.
     */
    List<TransactionResponse> findAll();
}
