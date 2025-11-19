package com.paymentchain.transaction.infrastructure.adapter.client;

import com.paymentchain.transaction.application.dto.CustomerInfo;
import com.paymentchain.transaction.application.port.out.CustomerClientPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

/**
 * HTTP Client Adapter: Implements CustomerClientPort.
 *
 * Infrastructure layer: calls Customer Service via WebClient.
 *
 * @author benas
 */
@Component
public class CustomerHttpClientAdapter implements CustomerClientPort {

    private static final Logger log = LoggerFactory.getLogger(CustomerHttpClientAdapter.class);

    private final WebClient webClient;
    private final String customerServiceUrl;

    public CustomerHttpClientAdapter(
            WebClient webClient,
            @Value("${customer.service.url:http://localhost:8081}") String customerServiceUrl) {
        this.webClient = webClient;
        this.customerServiceUrl = customerServiceUrl;
    }

    @Override
    public Optional<CustomerInfo> getCustomerByIban(String iban) {
        log.debug("Fetching customer by IBAN: {}", iban);

        try {
            CustomerInfo customer = webClient
                .get()
                .uri(customerServiceUrl + "/api/customers/iban/{iban}", iban)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> {
                        log.warn("Error fetching customer: status={}, iban={}",
                            response.statusCode(), iban);
                        return Mono.empty();
                    }
                )
                .bodyToMono(CustomerInfo.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(error -> {
                    log.error("Failed to fetch customer: iban={}, error={}",
                        iban, error.getMessage());
                    return Mono.empty();
                })
                .block();

            if (customer != null) {
                log.debug("Customer found: id={}, name={}", customer.id(), customer.name());
                return Optional.of(customer);
            } else {
                log.debug("Customer not found: iban={}", iban);
                return Optional.empty();
            }

        } catch (Exception e) {
            log.error("Exception fetching customer: iban={}, error={}",
                iban, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public boolean customerExists(String iban) {
        log.debug("Checking if customer exists: iban={}", iban);

        try {
            Boolean exists = webClient
                .head()
                .uri(customerServiceUrl + "/api/customers/iban/{iban}", iban)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .timeout(Duration.ofSeconds(3))
                .onErrorReturn(false)
                .block();

            log.debug("Customer exists check: iban={}, exists={}", iban, exists);
            return Boolean.TRUE.equals(exists);

        } catch (Exception e) {
            log.error("Exception checking customer existence: iban={}, error={}",
                iban, e.getMessage());
            return false;
        }
    }
}
