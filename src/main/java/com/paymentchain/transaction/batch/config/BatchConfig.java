package com.paymentchain.transaction.batch.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

/**
 * Enable Spring Batch processing.
 *
 * Creates necessary batch infrastructure beans:
 * - JobRepository
 * - JobLauncher
 * - JobRegistry
 * - PlatformTransactionManager
 *
 * @author benas
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {
    // Spring Boot auto-configures batch infrastructure
    // with spring-boot-starter-batch
}
