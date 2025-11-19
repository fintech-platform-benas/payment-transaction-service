package com.paymentchain.transaction.infrastructure.config;

import com.paymentchain.transaction.application.port.in.CreateTransactionUseCase;
import com.paymentchain.transaction.application.port.in.FindTransactionUseCase;
import com.paymentchain.transaction.application.port.in.SettleTransactionUseCase;
import com.paymentchain.transaction.application.port.in.UpdateTransactionUseCase;
import com.paymentchain.transaction.application.port.out.CustomerClientPort;
import com.paymentchain.transaction.application.port.out.EventPublisherPort;
import com.paymentchain.transaction.application.port.out.TransactionRepositoryPort;
import com.paymentchain.transaction.application.service.CreateTransactionService;
import com.paymentchain.transaction.application.service.FindTransactionService;
import com.paymentchain.transaction.application.service.SettleTransactionService;
import com.paymentchain.transaction.application.service.UpdateTransactionService;
import com.paymentchain.transaction.application.service.ValidatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bean Configuration for Hexagonal Architecture.
 *
 * Wires together Use Cases with their dependencies (Ports OUT).
 *
 * @author benas
 */
@Configuration
public class BeanConfig {

    /**
     * Create Transaction Use Case.
     */
    @Bean
    public CreateTransactionUseCase createTransactionUseCase(
            TransactionRepositoryPort repository,
            EventPublisherPort eventPublisher,
            CustomerClientPort customerClient,
            ValidatorFactory validatorFactory) {

        return new CreateTransactionService(
            repository,
            eventPublisher,
            customerClient,
            validatorFactory
        );
    }

    /**
     * Find Transaction Use Case.
     */
    @Bean
    public FindTransactionUseCase findTransactionUseCase(
            TransactionRepositoryPort repository,
            CustomerClientPort customerClient) {

        return new FindTransactionService(repository, customerClient);
    }

    /**
     * Update Transaction Use Case.
     */
    @Bean
    public UpdateTransactionUseCase updateTransactionUseCase(
            TransactionRepositoryPort repository) {

        return new UpdateTransactionService(repository);
    }

    /**
     * Settle Transaction Use Case.
     */
    @Bean
    public SettleTransactionUseCase settleTransactionUseCase(
            TransactionRepositoryPort repository,
            EventPublisherPort eventPublisher) {

        return new SettleTransactionService(repository, eventPublisher);
    }
}
