package com.jakubstaniszewski.evcharging.backend.application.dto;

import java.time.Instant;

/**
 * Result of finding the optimal continuous charging window.
 *
 * @param startTime             inclusive UTC start timestamp
 * @param endTime               exclusive UTC end timestamp
 * @param cleanEnergyPercentage mean clean energy share percentage in the window
 * @param partialData           whether the underlying forecast horizon had incomplete data
 */
public record ChargingWindowResult(
        Instant startTime,
        Instant endTime,
        double cleanEnergyPercentage,
        boolean partialData
) {
}
