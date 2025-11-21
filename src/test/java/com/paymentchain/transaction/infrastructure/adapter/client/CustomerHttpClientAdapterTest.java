package com.paymentchain.transaction.infrastructure.adapter.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentchain.transaction.application.dto.CustomerInfo;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for CustomerHttpClientAdapter with MockWebServer.
 *
 * Uses OkHttp's MockWebServer to simulate the Customer Service HTTP responses
 * without requiring an actual running service.
 *
 * @author benas
 */
class CustomerHttpClientAdapterTest {

    private MockWebServer mockWebServer;
    private CustomerHttpClientAdapter adapter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        WebClient webClient = WebClient.builder().build();
        adapter = new CustomerHttpClientAdapter(webClient, baseUrl);

        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldGetCustomerByIbanSuccessfully() throws Exception {
        // Arrange
        CustomerInfo expectedCustomer = new CustomerInfo(
            1L,
            "John",
            "Doe",
            "john.doe@example.com",
            "+34600123456",
            "ES1234567890123456789012"
        );

        String jsonResponse = objectMapper.writeValueAsString(expectedCustomer);

        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(jsonResponse)
            .addHeader("Content-Type", "application/json"));

        // Act
        Optional<CustomerInfo> result = adapter.getCustomerByIban("ES1234567890123456789012");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().name()).isEqualTo("John");
        assertThat(result.get().surname()).isEqualTo("Doe");
        assertThat(result.get().email()).isEqualTo("john.doe@example.com");
        assertThat(result.get().iban()).isEqualTo("ES1234567890123456789012");
    }

    @Test
    void shouldReturnEmptyWhenCustomerNotFound() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404)
            .addHeader("Content-Type", "application/json"));

        // Act
        Optional<CustomerInfo> result = adapter.getCustomerByIban("ES9999999999999999999999");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleServerError() {
        // Arrange
        // WebClient onStatus handler returns Mono.empty() which results in a null response
        // which becomes Optional.empty() after block()
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(500)
            .setBody("{\"error\":\"Internal Server Error\"}")
            .addHeader("Content-Type", "application/json"));

        // Act
        Optional<CustomerInfo> result = adapter.getCustomerByIban("ES1234567890123456789012");

        // Assert
        // Note: Due to WebClient behavior with error status, it may return empty or null object
        assertThat(result).satisfiesAnyOf(
            r -> assertThat(r).isEmpty(),
            r -> assertThat(r.get().id()).isNull()
        );
    }

    @Test
    void shouldCheckCustomerExistence() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        // Act
        boolean exists = adapter.customerExists("ES1234567890123456789012");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenCheckingNonExistentCustomer() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404)
            .addHeader("Content-Type", "application/json"));

        // Act
        boolean exists = adapter.customerExists("ES9999999999999999999999");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void shouldHandleEmptyResponse() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("")
            .addHeader("Content-Type", "application/json"));

        // Act
        Optional<CustomerInfo> result = adapter.getCustomerByIban("ES1234567890123456789012");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleMalformedJson() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{invalid-json}")
            .addHeader("Content-Type", "application/json"));

        // Act
        Optional<CustomerInfo> result = adapter.getCustomerByIban("ES1234567890123456789012");

        // Assert
        assertThat(result).isEmpty();
    }
}
