package com.paymentchain.transaction.batch.processor;

import com.paymentchain.transaction.batch.model.TransactionSummaryDto;
import com.paymentchain.transaction.infrastructure.adapter.persistence.TransactionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Processor: Aplica lógica de negocio a cada transacción.
 *
 * - Calcula comisiones (fee)
 * - Calcula total (amount + fee)
 * - Identifica transacciones de alto valor (> 1000)
 * - Filtra transacciones inválidas (retorna null)
 *
 * @author benas
 */
@Component
public class TransactionItemProcessor implements ItemProcessor<TransactionEntity, TransactionSummaryDto> {

    private static final Logger log = LoggerFactory.getLogger(TransactionItemProcessor.class);

    private static final BigDecimal FEE_PERCENTAGE = new BigDecimal("0.015"); // 1.5%
    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("1000");

    @Override
    public TransactionSummaryDto process(TransactionEntity transaction) {
        log.debug("Processing transaction: id={}, reference={}",
            transaction.getId(), transaction.getReference());

        // Filtrar transacciones inválidas (retornar null = skip)
        if (transaction.getStatus() == TransactionEntity.TransactionStatusEnum.CANCELLED) {
            log.debug("Skipping cancelled transaction: {}", transaction.getReference());
            return null; // Transacciones canceladas no van al CSV
        }

        // Calcular comisión (1.5% del monto)
        BigDecimal calculatedFee = transaction.getAmount()
                .multiply(FEE_PERCENTAGE)
                .setScale(2, RoundingMode.HALF_UP);

        // Calcular total
        BigDecimal totalAmount = transaction.getAmount().add(calculatedFee);

        // Identificar alto valor
        boolean isHighValue = transaction.getAmount().compareTo(HIGH_VALUE_THRESHOLD) > 0;

        // Crear DTO con datos procesados
        TransactionSummaryDto summary = new TransactionSummaryDto(
            transaction.getId(),
            transaction.getReference(),
            transaction.getAccountIban(),
            transaction.getTransactionDate(),
            transaction.getAmount(),
            transaction.getCurrency(),
            calculatedFee,
            totalAmount,
            transaction.getStatus().name(),
            transaction.getType().name(),
            isHighValue
        );

        log.debug("Processed transaction: reference={}, totalAmount={}, highValue={}",
            summary.getReference(), summary.getTotalAmount(), summary.isHighValue());

        return summary;
    }
}
