package com.paymentchain.transaction.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger Configuration.
 *
 * @author benas
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI transactionServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Payment Transaction Service API")
                .description("Hexagonal Architecture - Transaction Management Microservice")
                .version("2.0.0")
                .contact(new Contact()
                    .name("Payment Platform Team")
                    .email("benas@paymentchain.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
