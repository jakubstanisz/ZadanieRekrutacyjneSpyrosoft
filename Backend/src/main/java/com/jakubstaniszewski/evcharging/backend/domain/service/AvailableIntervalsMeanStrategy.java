package com.jakubstaniszewski.evcharging.backend.domain.service;

import com.jakubstaniszewski.evcharging.backend.domain.model.energy.DailyEnergyMix;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.DataCompletenessStatus;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.EnergySource;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.GenerationInterval;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Calculates daily energy mix averages solely from available 30-minute intervals.
 * <p>
 * Completeness policy:
 * <ul>
 *     <li>0 intervals -> {@link DataCompletenessStatus#UNAVAILABLE} with 0.0% shares</li>
 *     <li>&lt; 90% of expected intervals -> {@link DataCompletenessStatus#PARTIAL}</li>
 *     <li>&ge; 90% of expected intervals -> {@link DataCompletenessStatus#COMPLETE}</li>
 * </ul>
 */
public final class AvailableIntervalsMeanStrategy implements CleanEnergyCalculationStrategy {

    public static final int EXPECTED_INTERVALS_PER_DAY = 48;
    public static final double COMPLETENESS_THRESHOLD = 0.90;

    @Override
    public DailyEnergyMix calculateDailyMix(LocalDate date, List<GenerationInterval> intervals, int expectedIntervals) {
        Objects.requireNonNull(date, "Date must not be null");
        if (expectedIntervals <= 0) {
            throw new IllegalArgumentException("Expected intervals must be positive");
        }

        List<GenerationInterval> safeIntervals = intervals == null ? List.of() : intervals;
        if (safeIntervals.isEmpty()) {
            return unavailableDay(date, expectedIntervals);
        }

        Map<EnergySource, Double> averageShares = computeAverageShares(safeIntervals);
        double cleanEnergyAverage = roundToTwoDecimals(
                safeIntervals.stream()
                        .mapToDouble(GenerationInterval::cleanEnergyPercentage)
                        .average()
                        .orElse(0.0)
        );

        int availableCount = safeIntervals.size();
        DataCompletenessStatus status = resolveStatus(availableCount, expectedIntervals);

        return new DailyEnergyMix(
                date,
                averageShares,
                cleanEnergyAverage,
                availableCount,
                expectedIntervals,
                status
        );
    }

    private DailyEnergyMix unavailableDay(LocalDate date, int expectedIntervals) {
        Map<EnergySource, Double> zeroShares = new EnumMap<>(EnergySource.class);
        for (EnergySource source : EnergySource.values()) {
            zeroShares.put(source, 0.0);
        }
        return new DailyEnergyMix(date, zeroShares, 0.0, 0, expectedIntervals, DataCompletenessStatus.UNAVAILABLE);
    }

    private Map<EnergySource, Double> computeAverageShares(List<GenerationInterval> intervals) {
        Map<EnergySource, Double> averages = new EnumMap<>(EnergySource.class);
        for (EnergySource source : EnergySource.values()) {
            double avg = intervals.stream()
                    .mapToDouble(interval -> interval.shareOf(source))
                    .average()
                    .orElse(0.0);
            averages.put(source, roundToTwoDecimals(avg));
        }
        return averages;
    }

    private DataCompletenessStatus resolveStatus(int availableCount, int expectedIntervals) {
        double coverageRatio = (double) availableCount / expectedIntervals;
        return coverageRatio >= COMPLETENESS_THRESHOLD
                ? DataCompletenessStatus.COMPLETE
                : DataCompletenessStatus.PARTIAL;
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
