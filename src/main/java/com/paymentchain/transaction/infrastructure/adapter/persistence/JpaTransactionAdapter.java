package com.paymentchain.transaction.infrastructure.adapter.persistence;

import com.paymentchain.transaction.application.port.out.TransactionRepositoryPort;
import com.paymentchain.transaction.domain.model.Transaction;
import com.paymentchain.transaction.domain.model.TransactionStatus;
import com.paymentchain.transaction.infrastructure.adapter.persistence.mapper.TransactionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA Adapter: Implements TransactionRepositoryPort.
 *
 * Infrastructure layer: bridges domain port with Spring Data JPA.
 *
 * @author benas
 */
@Component
public class JpaTransactionAdapter implements TransactionRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(JpaTransactionAdapter.class);

    private final JpaTransactionRepository jpaRepository;
    private final TransactionMapper mapper;

    public JpaTransactionAdapter(
            JpaTransactionRepository jpaRepository,
            TransactionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        log.debug("Saving transaction: reference={}", transaction.getReference());

        TransactionEntity entity;

        if (transaction.getId() != null) {
            // Update existing entity
            entity = jpaRepository.findById(transaction.getId())
                .orElseThrow(() -> new IllegalStateException(
                    "Transaction not found for update: " + transaction.getId()
                ));
            mapper.updateEntityFromDomain(transaction, entity);
        } else {
            // Create new entity
            entity = mapper.toEntity(transaction);
        }

        TransactionEntity saved = jpaRepository.save(entity);
        log.debug("Transaction saved: id={}", saved.getId());

        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        log.debug("Finding transaction by id: {}", id);

        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<Transaction> findByReference(String reference) {
        log.debug("Finding transaction by reference: {}", reference);

        return jpaRepository.findByReference(reference)
            .map(mapper::toDomain);
    }

    @Override
    public List<Transaction> findByAccountIban(String iban) {
        log.debug("Finding transactions by IBAN: {}", iban);

        return jpaRepository.findByAccountIban(iban).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByStatus(TransactionStatus status) {
        log.debug("Finding transactions by status: {}", status);

        TransactionEntity.TransactionStatusEnum statusEnum =
            TransactionEntity.TransactionStatusEnum.valueOf(status.name());

        return jpaRepository.findByStatus(statusEnum).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByDateBetween(LocalDateTime from, LocalDateTime to) {
        log.debug("Finding transactions between {} and {}", from, to);

        return jpaRepository.findByDateBetween(from, to).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findAll() {
        log.debug("Finding all transactions");

        return jpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByReference(String reference) {
        log.debug("Checking if transaction exists by reference: {}", reference);

        return jpaRepository.existsByReference(reference);
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting transaction by id: {}", id);

        jpaRepository.deleteById(id);
    }
}
