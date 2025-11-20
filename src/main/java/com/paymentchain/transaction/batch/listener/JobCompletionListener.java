package com.paymentchain.transaction.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Listener: Job lifecycle events.
 *
 * - beforeJob: Log inicio
 * - afterJob: Log resultado (success/failure)
 *
 * @author benas
 */
@Component
public class JobCompletionListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("🚀 Starting Job: {} at {}",
            jobExecution.getJobInstance().getJobName(),
            jobExecution.getStartTime());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            long durationMs = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis();
            log.info("✅ Job COMPLETED: {} - Duration: {}ms - Read: {} - Written: {}",
                jobExecution.getJobInstance().getJobName(),
                durationMs,
                jobExecution.getStepExecutions().stream()
                    .mapToLong(step -> step.getReadCount())
                    .sum(),
                jobExecution.getStepExecutions().stream()
                    .mapToLong(step -> step.getWriteCount())
                    .sum());
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.error("❌ Job FAILED: {} - Errors: {}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getAllFailureExceptions());
        }
    }
}
