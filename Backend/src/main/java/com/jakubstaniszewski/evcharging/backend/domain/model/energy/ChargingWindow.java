package com.jakubstaniszewski.evcharging.backend.domain.model.energy;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents the optimal continuous charging window found across forecast intervals.
 *
 * @param startTime             inclusive UTC start timestamp of the window
 * @param endTime               exclusive UTC end timestamp of the window
 * @param cleanEnergyPercentage mean clean energy share percentage across all intervals in the window
 * @param partialData           {@code true} if the underlying forecast horizon had fewer intervals than expected
 */
public record ChargingWindow(
        Instant startTime,
        Instant endTime,
        double cleanEnergyPercentage,
        boolean partialData
) {

    /**
     * Validates window boundary timestamps.
     */
    public ChargingWindow {
        Objects.requireNonNull(startTime, "Window start time must not be null");
        Objects.requireNonNull(endTime, "Window end time must not be null");
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Window end time must be after start time");
        }
    }
}
