package com.paymentchain.transaction.infrastructure.adapter.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentchain.domain.model.event.DomainEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for KafkaEventPublisherAdapter with EmbeddedKafka.
 *
 * @author benas
 */
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(
    partitions = 1,
    topics = {"transaction.created.test", "test.topic"},
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9093",
        "port=9093"
    }
)
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.cloud.config.enabled=false",
    "spring.cloud.config.import-check.enabled=false",
    "spring.flyway.enabled=false",
    "spring.batch.job.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class KafkaEventPublisherAdapterTest {

    @Autowired
    private KafkaEventPublisherAdapter adapter;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ObjectMapper objectMapper;

    private KafkaMessageListenerContainer<String, String> container;
    private BlockingQueue<ConsumerRecord<String, String>> records;

    @BeforeEach
    void setUp() {
        // Configure consumer
        Map<String, Object> consumerProps = Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
            ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + UUID.randomUUID(),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        );

        DefaultKafkaConsumerFactory<String, String> consumerFactory =
            new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProps = new ContainerProperties("transaction.created.test", "test.topic");
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProps);

        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);

        container.start();
        // Wait for assignment: 2 topics * 1 partition per topic = 2 partitions
        ContainerTestUtils.waitForAssignment(container, 2);

        // Clear any messages from previous tests
        records.clear();
    }

    @AfterEach
    void tearDown() {
        if (container != null) {
            container.stop();
        }
    }

    @Test
    void shouldPublishEventToKafkaWithCorrectKey() throws Exception {
        // Arrange
        TestDomainEvent event = new TestDomainEvent("Test transaction created");

        String expectedKey = "ES1234567890123456789012";

        // Act
        adapter.publishToTopic("test.topic", expectedKey, event);

        // Assert
        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);

        assertThat(received).isNotNull();
        assertThat(received.topic()).isEqualTo("test.topic");
        assertThat(received.key()).isEqualTo(expectedKey);
        assertThat(received.value()).isNotNull();
        assertThat(received.value()).contains("TestDomainEvent");
        assertThat(received.value()).contains("Test transaction created");
    }

    @Test
    void shouldSerializeEventCorrectly() throws Exception {
        // Arrange
        TestDomainEvent event = new TestDomainEvent("Transaction settled successfully");

        // Act
        adapter.publishToTopic("test.topic", "key-456", event);

        // Assert
        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);

        assertThat(received).isNotNull();
        String payload = received.value();

        // Verify JSON structure
        assertThat(payload).contains("\"eventId\"");
        assertThat(payload).contains("\"eventType\":\"TestDomainEvent\"");
        assertThat(payload).contains("\"description\":\"Transaction settled successfully\"");
    }

    @Test
    void shouldPublishMultipleEventsWithDifferentKeys() throws Exception {
        // Arrange
        TestDomainEvent event1 = new TestDomainEvent("First transaction");
        TestDomainEvent event2 = new TestDomainEvent("Second transaction");

        // Act
        adapter.publishToTopic("test.topic", "key-001", event1);
        adapter.publishToTopic("test.topic", "key-002", event2);

        // Assert
        ConsumerRecord<String, String> received1 = records.poll(10, TimeUnit.SECONDS);
        ConsumerRecord<String, String> received2 = records.poll(10, TimeUnit.SECONDS);

        assertThat(received1).isNotNull();
        assertThat(received2).isNotNull();

        assertThat(received1.key()).isEqualTo("key-001");
        assertThat(received1.value()).contains("First transaction");

        assertThat(received2.key()).isEqualTo("key-002");
        assertThat(received2.value()).contains("Second transaction");
    }

    /**
     * Test implementation of DomainEvent for testing purposes.
     *
     * Note: DomainEvent is an abstract class with eventId, occurredOn, and eventType fields.
     * Jackson needs getters for all fields to serialize properly.
     */
    public static class TestDomainEvent extends DomainEvent {
        private final String description;

        public TestDomainEvent() {
            super();
            this.description = "Test event";
        }

        public TestDomainEvent(String description) {
            super();
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
