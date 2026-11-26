package com.jakubstaniszewski.evcharging.backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties defining allowed CORS origins for the standalone frontend.
 *
 * @param allowedOrigins list of allowed frontend origins
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        allowedOrigins = (allowedOrigins == null || allowedOrigins.isEmpty())
                ? List.of("http://localhost:5173", "http://localhost:3000")
                : List.copyOf(allowedOrigins);
    }
}
