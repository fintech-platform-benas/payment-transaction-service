package com.paymentchain.transaction.infrastructure.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Transaction.
 *
 * @author benas
 */
@Repository
public interface JpaTransactionRepository extends JpaRepository<TransactionEntity, Long> {

    /**
     * Find transaction by unique reference.
     */
    Optional<TransactionEntity> findByReference(String reference);

    /**
     * Find all transactions by account IBAN.
     */
    List<TransactionEntity> findByAccountIban(String accountIban);

    /**
     * Find all transactions by status.
     */
    List<TransactionEntity> findByStatus(TransactionEntity.TransactionStatusEnum status);

    /**
     * Find transactions between dates.
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.transactionDate BETWEEN :from AND :to ORDER BY t.transactionDate DESC")
    List<TransactionEntity> findByDateBetween(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    /**
     * Check if reference exists.
     */
    boolean existsByReference(String reference);

    /**
     * Find by IBAN and status.
     */
    List<TransactionEntity> findByAccountIbanAndStatus(
        String accountIban,
        TransactionEntity.TransactionStatusEnum status
    );

    /**
     * Find recent transactions (last N days).
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.transactionDate >= :since ORDER BY t.transactionDate DESC")
    List<TransactionEntity> findRecentTransactions(@Param("since") LocalDateTime since);
}
