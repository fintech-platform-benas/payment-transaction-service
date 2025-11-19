package com.paymentchain.transaction.domain.model;

import com.paymentchain.domain.model.valueobject.IBAN;
import com.paymentchain.domain.model.valueobject.Money;
import com.paymentchain.transaction.domain.exception.TransactionDomainException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Transaction domain model - Hexagonal Architecture.
 *
 * Pure domain logic without framework dependencies.
 *
 * @author benas
 */
public class Transaction {

    private Long id;
    private String reference;
    private IBAN accountIban;
    private LocalDateTime date;
    private Money amount;
    private Money fee;
    private String description;
    private TransactionStatus status;
    private TransactionType type;
    private String channel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Public no-args constructor for MapStruct (infrastructure layer)
    public Transaction() {
    }

    /**
     * Factory method para crear nueva transacción.
     */
    public static Transaction create(
            String reference,
            IBAN accountIban,
            Money amount,
            TransactionType type,
            String channel) {

        Transaction transaction = new Transaction();
        transaction.reference = reference;
        transaction.accountIban = accountIban;
        transaction.amount = amount;
        transaction.type = type;
        transaction.channel = channel;
        transaction.status = TransactionStatus.PENDING;
        transaction.date = LocalDateTime.now();
        transaction.createdAt = LocalDateTime.now();
        transaction.fee = Money.zero(amount.getCurrency());

        return transaction;
    }

    /**
     * Validar transacción (domain rules).
     */
    public void validate() {
        validateReference();
        validateDate();
        validateAmount();
    }

    private void validateReference() {
        if (reference == null || reference.isBlank()) {
            throw new TransactionDomainException("Reference cannot be blank");
        }
        if (reference.length() > 50) {
            throw new TransactionDomainException("Reference too long (max 50 chars)");
        }
    }

    private void validateDate() {
        if (date == null) {
            throw new TransactionDomainException("Date cannot be null");
        }
        if (date.isAfter(LocalDateTime.now())) {
            throw new TransactionDomainException("Transaction date cannot be in the future");
        }
    }

    private void validateAmount() {
        if (amount == null) {
            throw new TransactionDomainException("Amount cannot be null");
        }
        if (amount.isZero()) {
            throw new TransactionDomainException("Amount cannot be zero");
        }
    }

    /**
     * Settle transaction (cambiar estado a SETTLED).
     */
    public void settle() {
        if (status != TransactionStatus.PENDING) {
            throw new TransactionDomainException(
                "Cannot settle transaction in status: " + status
            );
        }
        this.status = TransactionStatus.SETTLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Cancel transaction.
     */
    public void cancel(String reason) {
        if (status == TransactionStatus.SETTLED) {
            throw new TransactionDomainException("Cannot cancel settled transaction");
        }
        this.status = TransactionStatus.CANCELLED;
        this.description = (description != null ? description + " | " : "") + "Cancelled: " + reason;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Add fee to transaction.
     */
    public void addFee(Money feeAmount) {
        if (!feeAmount.getCurrency().equals(amount.getCurrency())) {
            throw new TransactionDomainException("Fee currency must match transaction currency");
        }
        this.fee = this.fee.add(feeAmount);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Get total amount (amount + fee).
     */
    public Money getTotalAmount() {
        return amount.add(fee);
    }

    /**
     * Check if transaction is high value (> 1000).
     */
    public boolean isHighValue() {
        return amount.isGreaterThan(Money.of(new BigDecimal("1000"), amount.getCurrency()));
    }

    // Getters (sin setters - immutability)
    public Long getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public IBAN getAccountIban() {
        return accountIban;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Money getAmount() {
        return amount;
    }

    public Money getFee() {
        return fee;
    }

    public String getDescription() {
        return description;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public TransactionType getType() {
        return type;
    }

    public String getChannel() {
        return channel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Public setters for infrastructure layer (DB reconstruction via MapStruct)
    // Note: These are only used by infrastructure adapters to rebuild domain from persistence
    public void setId(Long id) {
        this.id = id;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setAccountIban(IBAN accountIban) {
        this.accountIban = accountIban;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public void setFee(Money fee) {
        this.fee = fee;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    // Public setter for description (needed by application layer)
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(reference, that.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", accountIban=" + accountIban.getMasked() +
                ", amount=" + amount +
                ", status=" + status +
                '}';
    }
}
