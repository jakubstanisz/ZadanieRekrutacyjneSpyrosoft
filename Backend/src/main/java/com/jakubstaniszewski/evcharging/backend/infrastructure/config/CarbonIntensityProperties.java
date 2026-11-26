package com.jakubstaniszewski.evcharging.backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for the external UK Carbon Intensity API client.
 *
 * @param baseUrl        base URL of the Carbon Intensity API
 * @param connectTimeout HTTP connection timeout
 * @param readTimeout    HTTP read timeout
 */
@ConfigurationProperties(prefix = "app.carbon-intensity")
public record CarbonIntensityProperties(
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {
    public CarbonIntensityProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.carbonintensity.org.uk";
        }
        if (connectTimeout == null) {
            connectTimeout = Duration.ofSeconds(5);
        }
        if (readTimeout == null) {
            readTimeout = Duration.ofSeconds(5);
        }
    }
}
