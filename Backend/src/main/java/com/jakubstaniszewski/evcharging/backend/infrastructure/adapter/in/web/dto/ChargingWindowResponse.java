package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto;

import java.time.Instant;

/**
 * HTTP response representing the optimal clean-energy charging window.
 *
 * @param startTime             inclusive UTC start timestamp
 * @param endTime               exclusive UTC end timestamp
 * @param cleanEnergyPercentage mean clean energy percentage across the window
 * @param partialData           whether the underlying forecast horizon had incomplete data
 */
public record ChargingWindowResponse(
        Instant startTime,
        Instant endTime,
        double cleanEnergyPercentage,
        boolean partialData
) {
}
