package com.paymentchain.transaction.infrastructure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller.
 *
 * @author benas
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Health check API")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check", description = "Check if service is running")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "payment-transaction-service");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}
