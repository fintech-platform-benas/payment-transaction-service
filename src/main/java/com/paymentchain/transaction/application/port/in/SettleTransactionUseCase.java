package com.paymentchain.transaction.application.port.in;

import com.paymentchain.transaction.application.dto.TransactionResponse;

/**
 * Port IN: Settle (complete) transaction.
 *
 * @author benas
 */
public interface SettleTransactionUseCase {

    /**
     * Settle transaction (mark as completed).
     *
     * @param id Transaction ID
     * @return Settled transaction
     */
    TransactionResponse settleTransaction(Long id);

    /**
     * Cancel transaction.
     *
     * @param id Transaction ID
     * @param reason Cancellation reason
     * @return Cancelled transaction
     */
    TransactionResponse cancelTransaction(Long id, String reason);
}
