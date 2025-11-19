package com.paymentchain.transaction.application.port.in;

import com.paymentchain.transaction.application.dto.CreateTransactionCommand;
import com.paymentchain.transaction.application.dto.TransactionResponse;

/**
 * Port IN: Create new transaction.
 *
 * @author benas
 */
public interface CreateTransactionUseCase {

    /**
     * Create new transaction.
     *
     * Workflow:
     * 1. Validate command
     * 2. Create domain model
     * 3. Apply business rules (validators)
     * 4. Save transaction
     * 5. Publish event
     * 6. Return response
     *
     * @param command Transaction creation command
     * @return Created transaction response
     */
    TransactionResponse createTransaction(CreateTransactionCommand command);
}
