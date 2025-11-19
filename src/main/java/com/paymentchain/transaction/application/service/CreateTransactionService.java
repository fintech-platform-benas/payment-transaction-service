package com.paymentchain.transaction.application.service;

import com.paymentchain.common.exception.ResourceNotFoundException;
import com.paymentchain.domain.model.valueobject.Currency;
import com.paymentchain.domain.model.valueobject.IBAN;
import com.paymentchain.domain.model.valueobject.Money;
import com.paymentchain.events.transaction.TransactionCreatedEvent;
import com.paymentchain.transaction.application.dto.CreateTransactionCommand;
import com.paymentchain.transaction.application.dto.CustomerInfo;
import com.paymentchain.transaction.application.dto.TransactionResponse;
import com.paymentchain.transaction.application.port.in.CreateTransactionUseCase;
import com.paymentchain.transaction.application.port.out.CustomerClientPort;
import com.paymentchain.transaction.application.port.out.EventPublisherPort;
import com.paymentchain.transaction.application.port.out.TransactionRepositoryPort;
import com.paymentchain.transaction.domain.model.Transaction;
import com.paymentchain.transaction.domain.model.TransactionType;
import com.paymentchain.transaction.domain.service.TransactionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case: Create Transaction.
 *
 * Orchestrates:
 * 1. Validation (domain + business rules)
 * 2. Customer verification
 * 3. Persistence
 * 4. Event publishing
 *
 * @author benas
 */
@Service
@Transactional
public class CreateTransactionService implements CreateTransactionUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateTransactionService.class);

    private final TransactionRepositoryPort repository;
    private final EventPublisherPort eventPublisher;
    private final CustomerClientPort customerClient;
    private final ValidatorFactory validatorFactory;

    public CreateTransactionService(
            TransactionRepositoryPort repository,
            EventPublisherPort eventPublisher,
            CustomerClientPort customerClient,
            ValidatorFactory validatorFactory) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.customerClient = customerClient;
        this.validatorFactory = validatorFactory;
    }

    @Override
    public TransactionResponse createTransaction(CreateTransactionCommand command) {
        log.info("Creating transaction: reference={}, iban={}, amount={}",
            command.reference(), command.accountIban(), command.amount());

        // 1. Check duplicate reference
        if (repository.existsByReference(command.reference())) {
            throw new ResourceNotFoundException("Transaction with reference " + command.reference() + " already exists");
        }

        // 2. Verify customer exists
        CustomerInfo customer = customerClient.getCustomerByIban(command.accountIban())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Customer", "IBAN", command.accountIban()
            ));

        // 3. Create domain model with Value Objects
        IBAN iban = IBAN.of(command.accountIban());
        Money amount = Money.of(command.amount(), Currency.valueOf(command.currency()));
        TransactionType type = TransactionType.valueOf(command.type());

        Transaction transaction = Transaction.create(
            command.reference(),
            iban,
            amount,
            type,
            command.channel()
        );

        if (command.description() != null) {
            transaction.setDescription(command.description());
        }

        // 4. Validate domain rules
        transaction.validate();

        // 5. Apply business rules (Factory + Strategy)
        List<TransactionValidator> validators = validatorFactory.getValidators(transaction);
        log.debug("Applying {} validators", validators.size());

        for (TransactionValidator validator : validators) {
            log.debug("Executing validator: {}", validator.getValidatorName());
            validator.validate(transaction);
        }

        // 6. Save transaction (Port OUT)
        Transaction saved = repository.save(transaction);
        log.info("Transaction saved: id={}, reference={}", saved.getId(), saved.getReference());

        // 7. Publish event (Port OUT)
        TransactionCreatedEvent event = TransactionCreatedEvent.builder()
            .transactionId(saved.getId())
            .reference(saved.getReference())
            .accountIban(saved.getAccountIban().getValue())
            .amount(saved.getAmount().getAmount())
            .currency(saved.getAmount().getCurrency().name())
            .status(saved.getStatus().name())
            .channel(saved.getChannel())
            .description(saved.getDescription())
            .transactionDate(saved.getDate())
            .build();

        eventPublisher.publishToTopic("transaction.created", saved.getAccountIban().getValue(), event);
        log.info("Event published: transactionId={}", saved.getId());

        // 8. Return response
        return TransactionResponse.from(saved, customer);
    }
}
