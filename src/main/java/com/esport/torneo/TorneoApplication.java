package com.esport.torneo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Aplicación principal del sistema de gestión de torneos E-Sport.
 * 
 * Esta aplicación proporciona funcionalidades para:
 * - Gestión de torneos de videojuegos
 * - Venta de tickets virtuales con QR/código único
 * - Sistema de notificaciones y alertas
 * - Dashboard de monitoreo y métricas
 * - Integración con plataformas de streaming
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class TorneoApplication {

    /**
     * Método principal para iniciar la aplicación Spring Boot.
     * 
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(TorneoApplication.class, args);
    }
} 