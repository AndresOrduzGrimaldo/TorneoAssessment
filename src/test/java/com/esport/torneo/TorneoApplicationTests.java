package com.esport.torneo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests básicos de arranque de la aplicación.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 2024
 */
@SpringBootTest
@ActiveProfiles("test")
class TorneoApplicationTests {

	/**
	 * Verifica que el contexto de la aplicación se carga correctamente.
	 */
	@Test
	void contextLoads() {
		// Este test simplemente verifica que el contexto de Spring Boot
		// se carga sin errores. Es un test básico pero importante para
		// detectar problemas de configuración.
	}

	/**
	 * Verifica que la aplicación principal puede iniciar correctamente.
	 */
	@Test
	void applicationStarts() {
		// Test implícito que verifica que todas las beans se pueden
		// instanciar correctamente y no hay conflictos de dependencias.
		// Al usar @SpringBootTest, esto se ejecuta automáticamente.
	}
} 