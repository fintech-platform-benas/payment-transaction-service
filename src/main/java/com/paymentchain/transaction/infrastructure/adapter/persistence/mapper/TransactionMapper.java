package com.paymentchain.transaction.infrastructure.adapter.persistence.mapper;

import com.paymentchain.domain.model.valueobject.Currency;
import com.paymentchain.domain.model.valueobject.IBAN;
import com.paymentchain.domain.model.valueobject.Money;
import com.paymentchain.transaction.domain.model.Transaction;
import com.paymentchain.transaction.domain.model.TransactionStatus;
import com.paymentchain.transaction.domain.model.TransactionType;
import com.paymentchain.transaction.infrastructure.adapter.persistence.TransactionEntity;
import org.mapstruct.*;

/**
 * MapStruct Mapper: Domain Model ↔ JPA Entity.
 *
 * Handles conversion between rich domain model (with Value Objects)
 * and flat JPA entity (database representation).
 *
 * @author benas
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface TransactionMapper {

    /**
     * Convert JPA Entity to Domain Model.
     *
     * Custom mappings:
     * - accountIban: String → IBAN (Value Object)
     * - amount + currency → Money (Value Object)
     * - fee + currency → Money (Value Object)
     * - status: TransactionStatusEnum → TransactionStatus
     * - type: TransactionTypeEnum → TransactionType
     * - transactionDate → date
     */
    @Mapping(target = "accountIban", source = "accountIban", qualifiedByName = "stringToIban")
    @Mapping(target = "amount", source = "entity", qualifiedByName = "toMoney")
    @Mapping(target = "fee", source = "entity", qualifiedByName = "toFee")
    @Mapping(target = "status", source = "status", qualifiedByName = "enumToStatus")
    @Mapping(target = "type", source = "type", qualifiedByName = "enumToType")
    @Mapping(target = "date", source = "transactionDate")
    Transaction toDomain(TransactionEntity entity);

    /**
     * Convert Domain Model to JPA Entity.
     *
     * Custom mappings:
     * - accountIban: IBAN → String
     * - amount: Money → BigDecimal + String
     * - fee: Money → BigDecimal
     * - status: TransactionStatus → TransactionStatusEnum
     * - type: TransactionType → TransactionTypeEnum
     * - date → transactionDate
     */
    @Mapping(target = "accountIban", source = "accountIban.value")
    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "currency", source = "amount.currency", qualifiedByName = "currencyToString")
    @Mapping(target = "fee", source = "fee.amount")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToEnum")
    @Mapping(target = "type", source = "type", qualifiedByName = "typeToEnum")
    @Mapping(target = "transactionDate", source = "date")
    TransactionEntity toEntity(Transaction domain);

    /**
     * Update existing entity from domain model.
     * Used for updates to preserve entity identity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "accountIban", source = "accountIban.value")
    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "currency", source = "amount.currency", qualifiedByName = "currencyToString")
    @Mapping(target = "fee", source = "fee.amount")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToEnum")
    @Mapping(target = "type", source = "type", qualifiedByName = "typeToEnum")
    @Mapping(target = "transactionDate", source = "date")
    void updateEntityFromDomain(Transaction domain, @MappingTarget TransactionEntity entity);

    // ==================== CUSTOM MAPPINGS ====================

    /**
     * String → IBAN Value Object.
     */
    @Named("stringToIban")
    default IBAN stringToIban(String value) {
        return value != null ? IBAN.of(value) : null;
    }

    /**
     * Entity → Money Value Object (amount + currency).
     */
    @Named("toMoney")
    default Money toMoney(TransactionEntity entity) {
        if (entity == null || entity.getAmount() == null || entity.getCurrency() == null) {
            return null;
        }
        Currency currency = Currency.valueOf(entity.getCurrency());
        return Money.of(entity.getAmount(), currency);
    }

    /**
     * Entity → Money Value Object (fee + currency).
     */
    @Named("toFee")
    default Money toFee(TransactionEntity entity) {
        if (entity == null || entity.getCurrency() == null) {
            return null;
        }
        Currency currency = Currency.valueOf(entity.getCurrency());
        if (entity.getFee() == null) {
            return Money.zero(currency);
        }
        return Money.of(entity.getFee(), currency);
    }

    /**
     * TransactionStatusEnum → TransactionStatus.
     */
    @Named("enumToStatus")
    default TransactionStatus enumToStatus(TransactionEntity.TransactionStatusEnum statusEnum) {
        if (statusEnum == null) return null;
        return TransactionStatus.valueOf(statusEnum.name());
    }

    /**
     * TransactionTypeEnum → TransactionType.
     */
    @Named("enumToType")
    default TransactionType enumToType(TransactionEntity.TransactionTypeEnum typeEnum) {
        if (typeEnum == null) return null;
        return TransactionType.valueOf(typeEnum.name());
    }

    /**
     * TransactionStatus → TransactionStatusEnum.
     */
    @Named("statusToEnum")
    default TransactionEntity.TransactionStatusEnum statusToEnum(TransactionStatus status) {
        if (status == null) return null;
        return TransactionEntity.TransactionStatusEnum.valueOf(status.name());
    }

    /**
     * TransactionType → TransactionTypeEnum.
     */
    @Named("typeToEnum")
    default TransactionEntity.TransactionTypeEnum typeToEnum(TransactionType type) {
        if (type == null) return null;
        return TransactionEntity.TransactionTypeEnum.valueOf(type.name());
    }

    /**
     * Currency → String.
     */
    @Named("currencyToString")
    default String currencyToString(Currency currency) {
        return currency != null ? currency.name() : null;
    }
}
