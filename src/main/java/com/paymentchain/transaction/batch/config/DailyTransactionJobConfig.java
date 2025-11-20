package com.paymentchain.transaction.batch.config;

import com.paymentchain.transaction.batch.listener.JobCompletionListener;
import com.paymentchain.transaction.batch.model.TransactionSummaryDto;
import com.paymentchain.transaction.batch.processor.TransactionItemProcessor;
import com.paymentchain.transaction.infrastructure.adapter.persistence.TransactionEntity;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Job Configuration: Daily Transaction Processing.
 *
 * Job: dailyTransactionJob
 * Step: processTransactionsStep
 *   - Reader: JpaPagingItemReader (DB → TransactionEntity)
 *   - Processor: TransactionItemProcessor (Entity → DTO + logic)
 *   - Writer: FlatFileItemWriter (DTO → CSV)
 *
 * Chunk-oriented processing: 10 items per chunk.
 *
 * @author benas
 */
@Configuration
public class DailyTransactionJobConfig {

    /**
     * Step: Procesa transacciones del día.
     *
     * Chunk size: 10 (lee, procesa y escribe 10 transacciones a la vez)
     */
    @Bean
    public Step processTransactionsStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JpaPagingItemReader<TransactionEntity> transactionReader,
            TransactionItemProcessor processor,
            FlatFileItemWriter<TransactionSummaryDto> csvWriter) {

        return new StepBuilder("processTransactionsStep", jobRepository)
                .<TransactionEntity, TransactionSummaryDto>chunk(10, transactionManager)
                .reader(transactionReader)
                .processor(processor)
                .writer(csvWriter)
                .build();
    }

    /**
     * Job: Daily transaction processing.
     *
     * Ejecuta el step de procesamiento.
     */
    @Bean
    public Job dailyTransactionJob(
            JobRepository jobRepository,
            Step processTransactionsStep,
            JobCompletionListener listener) {

        return new JobBuilder("dailyTransactionJob", jobRepository)
                .listener(listener)
                .start(processTransactionsStep)
                .build();
    }
}
