package com.esport.torneo.presentation.controller;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Health check controller for monitoring application status.
 * 
 * Provides endpoints to check the health of the application
 * and its dependencies (database, cache, messaging).
 * 
 * @author Andrés Orduz
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Application health monitoring APIs")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    /**
     * Basic health check endpoint.
     */
    @GetMapping
    @Operation(
        summary = "Health check",
        description = "Basic health check to verify the application is running"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application is healthy"),
        @ApiResponse(responseCode = "503", description = "Application is unhealthy")
    })
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "Tournament Management System");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }

    /**
     * Detailed health check with dependencies.
     */
    @GetMapping("/detailed")
    @Operation(
        summary = "Detailed health check",
        description = "Comprehensive health check including database and other dependencies"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detailed health information"),
        @ApiResponse(responseCode = "503", description = "One or more dependencies are unhealthy")
    })
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        Map<String, Object> components = new HashMap<>();
        
        // Check database
        components.put("database", checkDatabase());
        
        // Overall status
        boolean allHealthy = components.values().stream()
                .allMatch(component -> "UP".equals(((Map<?, ?>) component).get("status")));
        
        health.put("status", allHealthy ? "UP" : "DOWN");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "Tournament Management System");
        health.put("version", "1.0.0");
        health.put("components", components);
        
        return ResponseEntity.ok(health);
    }

    /**
     * Readiness probe for Kubernetes.
     */
    @GetMapping("/ready")
    @Operation(
        summary = "Readiness probe",
        description = "Kubernetes readiness probe to check if the application is ready to serve traffic"
    )
    @ApiResponse(responseCode = "200", description = "Application is ready")
    public ResponseEntity<Map<String, String>> ready() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "READY");
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Liveness probe for Kubernetes.
     */
    @GetMapping("/live")
    @Operation(
        summary = "Liveness probe",
        description = "Kubernetes liveness probe to check if the application is alive"
    )
    @ApiResponse(responseCode = "200", description = "Application is alive")
    public ResponseEntity<Map<String, String>> live() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ALIVE");
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Check database connectivity.
     */
    private Map<String, Object> checkDatabase() {
        Map<String, Object> dbHealth = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                dbHealth.put("status", "UP");
                dbHealth.put("database", connection.getMetaData().getDatabaseProductName());
                dbHealth.put("url", connection.getMetaData().getURL());
            } else {
                dbHealth.put("status", "DOWN");
                dbHealth.put("error", "Database connection is not valid");
            }
        } catch (Exception e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
        }
        
        return dbHealth;
    }
} 