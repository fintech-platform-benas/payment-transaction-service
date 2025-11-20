package com.paymentchain.transaction.batch.reader;

import com.paymentchain.transaction.infrastructure.adapter.persistence.TransactionEntity;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Reader: Lee transacciones del día desde PostgreSQL.
 *
 * Usa JpaPagingItemReader para procesar en chunks (evita cargar todo en memoria).
 *
 * @author benas
 */
@Component
public class TransactionItemReader {

    /**
     * Reader configurado para leer transacciones del día actual.
     *
     * Chunk-oriented: lee en páginas de 10 registros.
     */
    @Bean
    public JpaPagingItemReader<TransactionEntity> transactionReader(
            EntityManagerFactory entityManagerFactory) {

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        return new JpaPagingItemReaderBuilder<TransactionEntity>()
                .name("transactionReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(
                    "SELECT t FROM TransactionEntity t " +
                    "WHERE t.transactionDate BETWEEN :startDate AND :endDate " +
                    "ORDER BY t.transactionDate ASC"
                )
                .parameterValues(Map.of(
                    "startDate", startOfDay,
                    "endDate", endOfDay
                ))
                .pageSize(10) // Lee 10 registros por página (chunk)
                .build();
    }
}
