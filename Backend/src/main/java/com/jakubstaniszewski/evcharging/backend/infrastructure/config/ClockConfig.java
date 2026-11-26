package com.jakubstaniszewski.evcharging.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Provides a centralized UTC {@link Clock} bean for deterministic time handling.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock utcClock() {
        return Clock.systemUTC();
    }
}
