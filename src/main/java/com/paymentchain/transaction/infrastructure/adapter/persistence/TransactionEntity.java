package com.paymentchain.transaction.infrastructure.adapter.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for Transaction.
 *
 * Infrastructure layer: maps domain model to database schema.
 *
 * @author benas
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_reference", columnList = "reference", unique = true),
    @Index(name = "idx_account_iban", columnList = "account_iban"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_date", columnList = "transaction_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @Column(name = "account_iban", nullable = false, length = 24)
    private String accountIban;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(precision = 19, scale = 4)
    private BigDecimal fee;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatusEnum status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionTypeEnum type;

    @Column(nullable = false, length = 50)
    private String channel;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enums for JPA
    public enum TransactionStatusEnum {
        PENDING, SETTLED, CANCELLED, FAILED
    }

    public enum TransactionTypeEnum {
        PAYMENT, REFUND, TRANSFER, DEPOSIT, WITHDRAWAL
    }
}
