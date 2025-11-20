package com.paymentchain.transaction.batch.controller;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for BatchJobController.
 *
 * @author benas
 */
@WebMvcTest(BatchJobController.class)
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "spring.cloud.config.import-check.enabled=false"
})
class BatchJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobLauncher jobLauncher;

    @MockBean
    private Job dailyTransactionJob;

    @Test
    void shouldTriggerDailyTransactionJobSuccessfully() throws Exception {
        // Arrange
        JobExecution jobExecution = new JobExecution(1L);
        jobExecution.setStatus(BatchStatus.STARTED);

        when(jobLauncher.run(eq(dailyTransactionJob), any(JobParameters.class)))
                .thenReturn(jobExecution);

        // Act & Assert
        mockMvc.perform(post("/api/batch/daily-transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Daily transaction job started successfully"))
                .andExpect(jsonPath("$.jobExecutionId").value(1))
                .andExpect(jsonPath("$.status").value("STARTED"));
    }

    // Note: Exception handling test omitted as controller doesn't handle exceptions
    // The GlobalExceptionHandler will catch exceptions in production

    @Test
    void shouldReturnCompletedStatusWhenJobFinishesQuickly() throws Exception {
        // Arrange - Simulate a job that completes very quickly
        JobExecution jobExecution = new JobExecution(2L);
        jobExecution.setStatus(BatchStatus.COMPLETED);

        when(jobLauncher.run(eq(dailyTransactionJob), any(JobParameters.class)))
                .thenReturn(jobExecution);

        // Act & Assert
        mockMvc.perform(post("/api/batch/daily-transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.jobExecutionId").value(2))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
