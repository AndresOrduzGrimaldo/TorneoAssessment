package com.esport.torneo.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

/**
 * Configuración de patrones de resiliencia.
 * 
 * Define configuraciones para Circuit Breaker, Retry y Timeout
 * que mejoran la tolerancia a fallos del sistema.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Configuration
public class ResilienceConfig {

    /**
     * Configuración de Circuit Breaker para servicios externos.
     * 
     * @return Circuit Breaker configurado
     */
    @Bean
    public CircuitBreaker externalServiceCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50) // 50% de fallos para abrir el circuito
            .waitDurationInOpenState(Duration.ofSeconds(30)) // Esperar 30s antes de intentar again
            .slidingWindowSize(20) // Ventana deslizante de 20 requests
            .minimumNumberOfCalls(10) // Mínimo 10 llamadas para calcular tasa de fallo
            .slowCallRateThreshold(50) // 50% de llamadas lentas para abrir
            .slowCallDurationThreshold(Duration.ofSeconds(3)) // Llamada lenta > 3s
            .permittedNumberOfCallsInHalfOpenState(5) // 5 llamadas en estado semi-abierto
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .build();

        return CircuitBreaker.of("externalService", config);
    }

    /**
     * Configuración de Circuit Breaker para base de datos.
     * 
     * @return Circuit Breaker configurado para BD
     */
    @Bean
    public CircuitBreaker databaseCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(60) // 60% de fallos para abrir
            .waitDurationInOpenState(Duration.ofSeconds(10)) // Esperar menos para BD
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .slowCallRateThreshold(60)
            .slowCallDurationThreshold(Duration.ofSeconds(2)) // BD debería ser más rápida
            .permittedNumberOfCallsInHalfOpenState(3)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .build();

        return CircuitBreaker.of("database", config);
    }

    /**
     * Configuración de Retry para operaciones transitorias.
     * 
     * @return Retry configurado
     */
    @Bean
    public Retry defaultRetry() {
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(3) // Máximo 3 intentos
            .waitDuration(Duration.ofMillis(500)) // Esperar 500ms entre intentos
            .retryOnException(throwable -> {
                // Reintentar solo en excepciones específicas
                return throwable instanceof java.sql.SQLException ||
                       throwable instanceof org.springframework.dao.DataAccessException ||
                       throwable instanceof java.net.ConnectException;
            })
            .build();

        return Retry.of("defaultRetry", config);
    }

    /**
     * Configuración de Retry para operaciones críticas.
     * 
     * @return Retry configurado para operaciones críticas
     */
    @Bean
    public Retry criticalOperationRetry() {
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(5) // Más intentos para operaciones críticas
            .waitDuration(Duration.ofMillis(1000))
            .retryOnException(throwable -> {
                // Más permisivo para operaciones críticas
                return !(throwable instanceof IllegalArgumentException) &&
                       !(throwable instanceof IllegalStateException);
            })
            .build();

        return Retry.of("criticalOperation", config);
    }

    /**
     * Configuración de Time Limiter para operaciones síncronas.
     * 
     * @return TimeLimiter configurado
     */
    @Bean
    public TimeLimiter defaultTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(5)) // Timeout de 5 segundos
            .cancelRunningFuture(true) // Cancelar el future si hay timeout
            .build();

        return TimeLimiter.of("defaultTimeLimiter", config);
    }

    /**
     * Configuración de Time Limiter para operaciones rápidas.
     * 
     * @return TimeLimiter para operaciones que deben ser rápidas
     */
    @Bean
    public TimeLimiter fastOperationTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(2)) // Timeout más estricto
            .cancelRunningFuture(true)
            .build();

        return TimeLimiter.of("fastOperation", config);
    }

    /**
     * Configuración de Time Limiter para operaciones lentas permitidas.
     * 
     * @return TimeLimiter para operaciones que pueden tomar más tiempo
     */
    @Bean
    public TimeLimiter slowOperationTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(30)) // Timeout más relajado para reportes, etc.
            .cancelRunningFuture(true)
            .build();

        return TimeLimiter.of("slowOperation", config);
    }
} 