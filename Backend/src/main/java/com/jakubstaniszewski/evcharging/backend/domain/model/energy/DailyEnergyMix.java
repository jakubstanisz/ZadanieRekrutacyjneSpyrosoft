package com.jakubstaniszewski.evcharging.backend.domain.model.energy;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

/**
 * Aggregated generation mix summary for a single UTC calendar day.
 *
 * @param date                  UTC date represented by this summary
 * @param averageFuelShares     mean percentage share per {@link EnergySource} across available intervals
 * @param cleanEnergyPercentage mean total clean energy percentage across available intervals
 * @param availableIntervals    number of 30-minute intervals actually received
 * @param expectedIntervals     expected number of 30-minute intervals in the day (48)
 * @param completenessStatus    data completeness classification for this day
 */
public record DailyEnergyMix(
        LocalDate date,
        Map<EnergySource, Double> averageFuelShares,
        double cleanEnergyPercentage,
        int availableIntervals,
        int expectedIntervals,
        DataCompletenessStatus completenessStatus
) {

    /**
     * Validates non-null invariants and preserves an immutable copy of average fuel shares.
     */
    public DailyEnergyMix {
        Objects.requireNonNull(date, "Date must not be null");
        Objects.requireNonNull(averageFuelShares, "Average fuel shares must not be null");
        Objects.requireNonNull(completenessStatus, "Completeness status must not be null");
        if (availableIntervals < 0) {
            throw new IllegalArgumentException("Available intervals must be non-negative");
        }
        if (expectedIntervals <= 0) {
            throw new IllegalArgumentException("Expected intervals must be positive");
        }
        averageFuelShares = Map.copyOf(averageFuelShares);
    }

    /**
     * Computes the fraction of expected intervals that were available.
     *
     * @return coverage ratio in {@code [0.0, 1.0+]}
     */
    public double coverageRatio() {
        return (double) availableIntervals / expectedIntervals;
    }
}
