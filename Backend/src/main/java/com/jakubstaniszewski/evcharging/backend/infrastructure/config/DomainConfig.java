package com.jakubstaniszewski.evcharging.backend.infrastructure.config;

import com.jakubstaniszewski.evcharging.backend.domain.service.AvailableIntervalsMeanStrategy;
import com.jakubstaniszewski.evcharging.backend.domain.service.ChargingWindowOptimizer;
import com.jakubstaniszewski.evcharging.backend.domain.service.CleanEnergyCalculationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers framework-free domain services and strategies as Spring beans.
 */
@Configuration
public class DomainConfig {

    @Bean
    public CleanEnergyCalculationStrategy cleanEnergyCalculationStrategy() {
        return new AvailableIntervalsMeanStrategy();
    }

    @Bean
    public ChargingWindowOptimizer chargingWindowOptimizer() {
        return new ChargingWindowOptimizer();
    }
}
