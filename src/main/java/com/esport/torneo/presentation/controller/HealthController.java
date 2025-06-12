package com.esport.torneo.presentation.controller;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador para endpoints de salud del sistema.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "API para verificar salud del sistema")
public class HealthController implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    private DataSource dataSource;

    @Operation(summary = "Verificar salud del sistema", description = "Endpoint básico para verificar que el sistema está funcionando")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sistema funcionando correctamente",
                    content = @Content(schema = @Schema(implementation = HealthResponse.class)))
    })
    @GetMapping("/status")
    public ResponseEntity<HealthResponse> getHealthStatus() {
        logger.debug("Verificando estado de salud del sistema");

        try {
            // Verificar conexión a base de datos
            boolean databaseHealthy = checkDatabaseConnection();
            
            // Verificar memoria disponible
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsagePercentage = (double) usedMemory / totalMemory * 100;

            HealthResponse response = new HealthResponse(
                "UP",
                LocalDateTime.now(),
                "Sistema funcionando correctamente",
                "1.0",
                databaseHealthy,
                memoryUsagePercentage
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error al verificar salud del sistema", e);
            
            HealthResponse response = new HealthResponse(
                "DOWN",
                LocalDateTime.now(),
                "Error en el sistema: " + e.getMessage(),
                "1.0",
                false,
                0.0
            );

            return ResponseEntity.status(503).body(response);
        }
    }

    @Operation(summary = "Verificar estado simple", description = "Endpoint simple para health checks")
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "pong");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Información del sistema", description = "Obtiene información básica del sistema")
    @GetMapping("/info")
    public ResponseEntity<SystemInfo> getSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        
        SystemInfo info = new SystemInfo(
            "Torneo E-Sport Management System",
            "1.0",
            System.getProperty("java.version"),
            runtime.availableProcessors(),
            runtime.totalMemory(),
            runtime.freeMemory(),
            LocalDateTime.now()
        );

        return ResponseEntity.ok(info);
    }

    @Override
    public Health health() {
        try {
            boolean databaseHealthy = checkDatabaseConnection();
            
            if (databaseHealthy) {
                return Health.up()
                    .withDetail("database", "UP")
                    .withDetail("timestamp", LocalDateTime.now())
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", "DOWN")
                    .withDetail("timestamp", LocalDateTime.now())
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("timestamp", LocalDateTime.now())
                .build();
        }
    }

    /**
     * Verifica la conexión a la base de datos.
     */
    private boolean checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // timeout de 5 segundos
        } catch (Exception e) {
            logger.error("Error al verificar conexión a base de datos", e);
            return false;
        }
    }

    /**
     * Respuesta de salud del sistema.
     */
    @Schema(description = "Respuesta de salud del sistema")
    public static class HealthResponse {
        @Schema(description = "Estado del sistema", example = "UP")
        private String status;
        
        @Schema(description = "Timestamp de la verificación")
        private LocalDateTime timestamp;
        
        @Schema(description = "Mensaje descriptivo")
        private String message;
        
        @Schema(description = "Versión del sistema")
        private String version;
        
        @Schema(description = "Estado de la base de datos")
        private boolean databaseHealthy;
        
        @Schema(description = "Porcentaje de uso de memoria")
        private double memoryUsagePercentage;

        public HealthResponse(String status, LocalDateTime timestamp, String message, 
                            String version, boolean databaseHealthy, double memoryUsagePercentage) {
            this.status = status;
            this.timestamp = timestamp;
            this.message = message;
            this.version = version;
            this.databaseHealthy = databaseHealthy;
            this.memoryUsagePercentage = memoryUsagePercentage;
        }

        // Getters y setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public boolean isDatabaseHealthy() { return databaseHealthy; }
        public void setDatabaseHealthy(boolean databaseHealthy) { this.databaseHealthy = databaseHealthy; }
        
        public double getMemoryUsagePercentage() { return memoryUsagePercentage; }
        public void setMemoryUsagePercentage(double memoryUsagePercentage) { this.memoryUsagePercentage = memoryUsagePercentage; }
    }

    /**
     * Información del sistema.
     */
    @Schema(description = "Información del sistema")
    public static class SystemInfo {
        @Schema(description = "Nombre de la aplicación")
        private String applicationName;
        
        @Schema(description = "Versión de la aplicación")
        private String version;
        
        @Schema(description = "Versión de Java")
        private String javaVersion;
        
        @Schema(description = "Número de procesadores")
        private int processors;
        
        @Schema(description = "Memoria total en bytes")
        private long totalMemory;
        
        @Schema(description = "Memoria libre en bytes")
        private long freeMemory;
        
        @Schema(description = "Timestamp de consulta")
        private LocalDateTime timestamp;

        public SystemInfo(String applicationName, String version, String javaVersion, 
                         int processors, long totalMemory, long freeMemory, LocalDateTime timestamp) {
            this.applicationName = applicationName;
            this.version = version;
            this.javaVersion = javaVersion;
            this.processors = processors;
            this.totalMemory = totalMemory;
            this.freeMemory = freeMemory;
            this.timestamp = timestamp;
        }

        // Getters y setters
        public String getApplicationName() { return applicationName; }
        public void setApplicationName(String applicationName) { this.applicationName = applicationName; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public String getJavaVersion() { return javaVersion; }
        public void setJavaVersion(String javaVersion) { this.javaVersion = javaVersion; }
        
        public int getProcessors() { return processors; }
        public void setProcessors(int processors) { this.processors = processors; }
        
        public long getTotalMemory() { return totalMemory; }
        public void setTotalMemory(long totalMemory) { this.totalMemory = totalMemory; }
        
        public long getFreeMemory() { return freeMemory; }
        public void setFreeMemory(long freeMemory) { this.freeMemory = freeMemory; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
} 