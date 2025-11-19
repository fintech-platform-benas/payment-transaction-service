package com.paymentchain.transaction.domain.exception;

import com.paymentchain.domain.exception.DomainException;

/**
 * Exception for transaction domain violations.
 *
 * @author benas
 */
public class TransactionDomainException extends DomainException {

    public TransactionDomainException(String message) {
        super(message);
    }

    public TransactionDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
