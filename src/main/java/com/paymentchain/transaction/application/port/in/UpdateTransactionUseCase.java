package com.paymentchain.transaction.application.port.in;

import com.paymentchain.transaction.application.dto.UpdateTransactionCommand;
import com.paymentchain.transaction.application.dto.TransactionResponse;

/**
 * Port IN: Update transaction.
 *
 * @author benas
 */
public interface UpdateTransactionUseCase {

    /**
     * Update transaction.
     *
     * @param id Transaction ID
     * @param command Update command
     * @return Updated transaction
     */
    TransactionResponse updateTransaction(Long id, UpdateTransactionCommand command);
}
