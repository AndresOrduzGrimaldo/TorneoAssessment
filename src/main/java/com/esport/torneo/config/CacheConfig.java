package com.esport.torneo.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuración de cache distribuido con Redis.
 * 
 * Define las configuraciones de cache para mejorar el rendimiento
 * de la aplicación mediante almacenamiento en cache de datos frecuentemente consultados.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configuración del template de Redis.
     * 
     * @param connectionFactory fábrica de conexiones Redis
     * @return template configurado de Redis
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Configurar serializadores
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        
        // Serializador para claves
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // Serializador para valores
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Manager de cache Redis personalizado.
     * 
     * @param connectionFactory fábrica de conexiones Redis
     * @return manager de cache configurado
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        
        // Configuración por defecto
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1)) // TTL por defecto: 1 hora
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            // Configuraciones específicas por cache
            .withCacheConfiguration("categories", 
                defaultConfig.entryTtl(Duration.ofMinutes(30)))
            .withCacheConfiguration("activeCategories", 
                defaultConfig.entryTtl(Duration.ofMinutes(15)))
            .withCacheConfiguration("games", 
                defaultConfig.entryTtl(Duration.ofMinutes(30)))
            .withCacheConfiguration("activeGames", 
                defaultConfig.entryTtl(Duration.ofMinutes(15)))
            .withCacheConfiguration("tournaments", 
                defaultConfig.entryTtl(Duration.ofMinutes(10)))
            .withCacheConfiguration("tournamentsByCategory", 
                defaultConfig.entryTtl(Duration.ofMinutes(5)))
            .withCacheConfiguration("tournamentsByGame", 
                defaultConfig.entryTtl(Duration.ofMinutes(5)))
            .withCacheConfiguration("tickets", 
                defaultConfig.entryTtl(Duration.ofMinutes(30)))
            .withCacheConfiguration("userTickets", 
                defaultConfig.entryTtl(Duration.ofMinutes(15)))
            .withCacheConfiguration("stats", 
                defaultConfig.entryTtl(Duration.ofMinutes(60)))
            .build();
    }
} 