package com.jakubstaniszewski.evcharging.backend.infrastructure.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Spring's caching abstraction backed by Caffeine configuration.
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
