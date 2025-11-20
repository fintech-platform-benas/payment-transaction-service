package com.paymentchain.transaction.batch.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller para ejecutar jobs batch manualmente.
 *
 * @author benas
 */
@RestController
@RequestMapping("/api/batch")
@Tag(name = "Batch Jobs", description = "Spring Batch job execution API")
public class BatchJobController {

    private final JobLauncher jobLauncher;
    private final Job dailyTransactionJob;

    public BatchJobController(JobLauncher jobLauncher, Job dailyTransactionJob) {
        this.jobLauncher = jobLauncher;
        this.dailyTransactionJob = dailyTransactionJob;
    }

    /**
     * Trigger manual: Daily transaction processing job.
     *
     * POST /api/batch/daily-transactions
     */
    @PostMapping("/daily-transactions")
    @Operation(summary = "Execute daily transaction processing job",
               description = "Processes all transactions from today and generates CSV report")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Job started successfully"),
        @ApiResponse(responseCode = "500", description = "Job execution failed")
    })
    public ResponseEntity<Map<String, Object>> runDailyTransactionJob() throws Exception {

        // Job parameters (deben ser únicos para cada ejecución)
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis()) // Timestamp único
                .toJobParameters();

        // Lanzar job
        JobExecution jobExecution = jobLauncher.run(dailyTransactionJob, jobParameters);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Daily transaction job started successfully");
        response.put("jobExecutionId", jobExecution.getId());
        response.put("status", jobExecution.getStatus().name());

        return ResponseEntity.ok(response);
    }
}
