package com.paymentchain.businessdomain.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.NONE,
	properties = {
		"eureka.client.enabled=false",
		"spring.cloud.config.enabled=false"
	}
)
@ActiveProfiles("test")
class TransactionApplicationTests {

	@Test
	void contextLoads() {
		// Test passes if Spring context loads successfully
	}

}
