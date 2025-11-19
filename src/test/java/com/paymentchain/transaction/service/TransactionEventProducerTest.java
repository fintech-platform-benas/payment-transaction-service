package com.paymentchain.transaction.service;

import com.paymentchain.transaction.events.TransactionCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionEventProducer.
 * Tests event publishing logic using Mockito.
 *
 * @author benas
 */
@ExtendWith(MockitoExtension.class)
class TransactionEventProducerTest {

    @Mock
    private KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

    private TransactionEventProducer producer;

    private static final String TOPIC_NAME = "transaction.created";

    @BeforeEach
    void setUp() {
        producer = new TransactionEventProducer(kafkaTemplate);
        ReflectionTestUtils.setField(producer, "topicName", TOPIC_NAME);
    }

    @Test
    void testPublish_SendsEventWithCorrectKey() {
        // Given
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                1L,
                "ES1234567890",
                100.0,
                LocalDateTime.now()
        );

        CompletableFuture<SendResult<String, TransactionCreatedEvent>> future =
                CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), any(TransactionCreatedEvent.class)))
                .thenReturn(future);

        // When
        producer.publish(event);

        // Then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<TransactionCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(TransactionCreatedEvent.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertEquals(TOPIC_NAME, topicCaptor.getValue());
        assertEquals("ES1234567890", keyCaptor.getValue(), "Key should be IBAN for partition ordering");
        assertEquals(event, eventCaptor.getValue());
    }

    @Test
    void testPublish_UsesIbanAsPartitionKey() {
        // Given
        String expectedIban = "ES9876543210";
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                2L,
                expectedIban,
                500.0,
                LocalDateTime.now()
        );

        CompletableFuture<SendResult<String, TransactionCreatedEvent>> future =
                CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), eq(expectedIban), any(TransactionCreatedEvent.class)))
                .thenReturn(future);

        // When
        producer.publish(event);

        // Then
        verify(kafkaTemplate).send(eq(TOPIC_NAME), eq(expectedIban), eq(event));
    }

    @Test
    void testPublish_HandlesMultipleEvents() {
        // Given
        TransactionCreatedEvent event1 = new TransactionCreatedEvent(1L, "ES111", 100.0, LocalDateTime.now());
        TransactionCreatedEvent event2 = new TransactionCreatedEvent(2L, "ES222", 200.0, LocalDateTime.now());

        CompletableFuture<SendResult<String, TransactionCreatedEvent>> future =
                CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), any(TransactionCreatedEvent.class)))
                .thenReturn(future);

        // When
        producer.publish(event1);
        producer.publish(event2);

        // Then
        verify(kafkaTemplate, times(2)).send(anyString(), anyString(), any(TransactionCreatedEvent.class));
    }
}
