package com.paymentchain.transaction.infrastructure.adapter.persistence.mapper;

import com.paymentchain.domain.model.valueobject.Currency;
import com.paymentchain.domain.model.valueobject.IBAN;
import com.paymentchain.domain.model.valueobject.Money;
import com.paymentchain.transaction.domain.model.Transaction;
import com.paymentchain.transaction.domain.model.TransactionStatus;
import com.paymentchain.transaction.domain.model.TransactionType;
import com.paymentchain.transaction.infrastructure.adapter.persistence.TransactionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for TransactionMapper (MapStruct).
 *
 * Note: This is a unit test that directly instantiates the mapper
 * using MapStruct's Mappers factory, avoiding the need for Spring context.
 *
 * @author benas
 */
class TransactionMapperTest {

    private TransactionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(TransactionMapper.class);
    }

    @Test
    void shouldMapDomainToEntity() {
        // Arrange
        Transaction domain = Transaction.create(
            "TX001",
            IBAN.of("ES1234567890123456789012"),
            Money.of(new BigDecimal("100.50"), Currency.EUR),
            TransactionType.PAYMENT,
            "WEB"
        );
        domain.setDescription("Test transaction");

        // Act
        TransactionEntity entity = mapper.toEntity(domain);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getReference()).isEqualTo("TX001");
        assertThat(entity.getAccountIban()).isEqualTo("ES1234567890123456789012");
        assertThat(entity.getAmount()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(entity.getCurrency()).isEqualTo("EUR");
        assertThat(entity.getType()).isEqualTo(TransactionEntity.TransactionTypeEnum.PAYMENT);
        assertThat(entity.getDescription()).isEqualTo("Test transaction");
    }

    @Test
    void shouldMapEntityToDomain() {
        // Arrange
        TransactionEntity entity = new TransactionEntity();
        entity.setId(1L);
        entity.setReference("TX001");
        entity.setAccountIban("ES1234567890123456789012");
        entity.setAmount(new BigDecimal("100.50"));
        entity.setCurrency("EUR");
        entity.setFee(BigDecimal.ZERO);
        entity.setStatus(TransactionEntity.TransactionStatusEnum.PENDING);
        entity.setType(TransactionEntity.TransactionTypeEnum.PAYMENT);
        entity.setChannel("WEB");
        entity.setTransactionDate(LocalDateTime.now());
        entity.setDescription("Test");

        // Act
        Transaction domain = mapper.toDomain(entity);

        // Assert
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getReference()).isEqualTo("TX001");
        assertThat(domain.getAccountIban()).isInstanceOf(IBAN.class);
        assertThat(domain.getAccountIban().getValue()).isEqualTo("ES1234567890123456789012");
        assertThat(domain.getAmount()).isInstanceOf(Money.class);
        assertThat(domain.getAmount().getAmount()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(domain.getAmount().getCurrency()).isEqualTo(Currency.EUR);
        assertThat(domain.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(domain.getType()).isEqualTo(TransactionType.PAYMENT);
    }

    @Test
    void shouldPreserveValueObjectsInRoundTrip() {
        // Arrange
        Transaction original = Transaction.create(
            "TX001",
            IBAN.of("ES1234567890123456789012"),
            Money.of(new BigDecimal("100.50"), Currency.EUR),
            TransactionType.PAYMENT,
            "WEB"
        );

        // Act
        TransactionEntity entity = mapper.toEntity(original);
        Transaction roundTrip = mapper.toDomain(entity);

        // Assert
        assertThat(roundTrip.getReference()).isEqualTo(original.getReference());
        assertThat(roundTrip.getAccountIban()).isEqualTo(original.getAccountIban());
        assertThat(roundTrip.getAmount()).isEqualTo(original.getAmount());
        assertThat(roundTrip.getType()).isEqualTo(original.getType());
        assertThat(roundTrip.getStatus()).isEqualTo(original.getStatus());
    }

    @Test
    void shouldMapStatusCorrectly() {
        // Arrange
        Transaction settled = Transaction.create(
            "TX_SETTLED",
            IBAN.of("ES1234567890123456789012"),
            Money.of(new BigDecimal("100"), Currency.EUR),
            TransactionType.PAYMENT,
            "WEB"
        );
        settled.settle();

        // Act
        TransactionEntity entity = mapper.toEntity(settled);
        Transaction mapped = mapper.toDomain(entity);

        // Assert
        assertThat(entity.getStatus()).isEqualTo(TransactionEntity.TransactionStatusEnum.SETTLED);
        assertThat(mapped.getStatus()).isEqualTo(TransactionStatus.SETTLED);
    }

    @Test
    void shouldMapFeeCorrectly() {
        // Arrange
        Transaction transaction = Transaction.create(
            "TX_FEE",
            IBAN.of("ES1234567890123456789012"),
            Money.of(new BigDecimal("100"), Currency.EUR),
            TransactionType.PAYMENT,
            "WEB"
        );
        transaction.addFee(Money.of(new BigDecimal("5.00"), Currency.EUR));

        // Act
        TransactionEntity entity = mapper.toEntity(transaction);
        Transaction mapped = mapper.toDomain(entity);

        // Assert
        assertThat(entity.getFee()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(mapped.getFee().getAmount()).isEqualByComparingTo(new BigDecimal("5.00"));
    }
}
