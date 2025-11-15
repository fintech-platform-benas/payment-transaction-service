package com.paymentchain.businessdomain.transaction.service;

import com.paymentchain.businessdomain.transaction.events.TransactionCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for publishing transaction events to Kafka.
 * Uses asynchronous sending with logging for success/failure.
 *
 * @author benas
 */
@Service
public class TransactionEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(TransactionEventProducer.class);

    private final KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

    @Value("${kafka.topic.transaction-created:transaction.created}")
    private String topicName;

    public TransactionEventProducer(KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes a transaction created event to Kafka.
     * Uses IBAN as the partition key to ensure ordering for same account.
     *
     * @param event The transaction created event
     */
    public void publish(TransactionCreatedEvent event) {
        logger.info("Publishing transaction created event: id={}, iban={}, amount={}",
                event.id(), event.iban(), event.amount());

        // Send with IBAN as key for partition ordering
        CompletableFuture<SendResult<String, TransactionCreatedEvent>> future =
                kafkaTemplate.send(topicName, event.iban(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Successfully published event to topic={}, partition={}, offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to publish event for transaction id={}: {}",
                        event.id(), ex.getMessage(), ex);
            }
        });
    }
}
