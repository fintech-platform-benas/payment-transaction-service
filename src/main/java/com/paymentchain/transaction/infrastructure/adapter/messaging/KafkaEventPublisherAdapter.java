package com.paymentchain.transaction.infrastructure.adapter.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentchain.domain.model.event.DomainEvent;
import com.paymentchain.transaction.application.port.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Adapter: Implements EventPublisherPort.
 *
 * Infrastructure layer: publishes domain events to Kafka topics.
 *
 * @author benas
 */
@Component
public class KafkaEventPublisherAdapter implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisherAdapter.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaEventPublisherAdapter(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        try {
            String topic = getTopicFromEvent(event);
            String key = event.getEventId(); // Use eventId as partition key
            String payload = objectMapper.writeValueAsString(event);

            log.info("Publishing event to Kafka: topic={}, key={}, eventType={}",
                topic, key, event.getEventType());

            CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(topic, key, payload);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event: topic={}, key={}, error={}",
                        topic, key, ex.getMessage(), ex);
                } else {
                    log.debug("Event published successfully: topic={}, partition={}, offset={}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            });

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", e.getMessage(), e);
            throw new RuntimeException("Event serialization failed", e);
        }
    }

    @Override
    public void publishToTopic(String topic, String key, DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            log.info("Publishing event to specific topic: topic={}, key={}, eventType={}",
                topic, key, event.getEventType());

            CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(topic, key, payload);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event: topic={}, key={}, error={}",
                        topic, key, ex.getMessage(), ex);
                } else {
                    log.debug("Event published successfully: topic={}, partition={}, offset={}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            });

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", e.getMessage(), e);
            throw new RuntimeException("Event serialization failed", e);
        }
    }

    /**
     * Determine topic from event type.
     * Convention: transaction.{eventType}
     */
    private String getTopicFromEvent(DomainEvent event) {
        String eventType = event.getEventType();

        // Convert: TransactionCreatedEvent → transaction.created
        if (eventType.endsWith("Event")) {
            eventType = eventType.substring(0, eventType.length() - 5);
        }

        // TransactionCreated → transaction.created
        String[] parts = eventType.split("(?=\\p{Upper})");
        StringBuilder topic = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                if (i > 0) {
                    topic.append(".");
                }
                topic.append(parts[i].toLowerCase());
            }
        }

        return topic.toString();
    }
}
