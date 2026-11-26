package com.jakubstaniszewski.evcharging.backend.domain.service;

import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingValidationException;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.ChargingWindow;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.GenerationInterval;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Pure domain service locating the cleanest continuous charging window on a continuous UTC timeline.
 * <p>
 * Operates over 30-minute intervals ({@code durationHours * 2} contiguous slots), naturally allowing
 * windows that cross midnight. In case of an exact tie in clean energy percentage, the earliest
 * window is selected.
 */
public final class ChargingWindowOptimizer {

    public static final int MIN_DURATION_HOURS = 1;
    public static final int MAX_DURATION_HOURS = 6;
    private static final int SLOTS_PER_HOUR = 2;

    /**
     * Finds the optimal continuous charging window of {@code durationHours} hours.
     *
     * @param intervals         forecast generation intervals across the search horizon
     * @param durationHours     requested charging length in whole hours (1–6)
     * @param expectedIntervals total number of intervals expected in the full horizon (e.g. 96 for 48h)
     * @return the cleanest contiguous {@link ChargingWindow}, or empty if no contiguous window fits
     */
    public Optional<ChargingWindow> findBestWindow(
            List<GenerationInterval> intervals,
            int durationHours,
            int expectedIntervals
    ) {
        validateDuration(durationHours);
        if (expectedIntervals <= 0) {
            throw new IllegalArgumentException("Expected intervals must be positive");
        }

        int requiredSlots = durationHours * SLOTS_PER_HOUR;
        List<GenerationInterval> timeline = normalizeTimeline(intervals);
        if (timeline.size() < requiredSlots) {
            return Optional.empty();
        }

        int bestStartIndex = -1;
        double bestAverage = Double.NEGATIVE_INFINITY;

        for (int i = 0; i <= timeline.size() - requiredSlots; i++) {
            if (!isContiguousWindow(timeline, i, requiredSlots)) {
                continue;
            }
            double windowAverage = calculateWindowAverage(timeline, i, requiredSlots);
            // Strictly greater keeps the earliest chronological window in case of a tie.
            if (windowAverage > bestAverage) {
                bestAverage = windowAverage;
                bestStartIndex = i;
            }
        }

        if (bestStartIndex == -1) {
            return Optional.empty();
        }

        Instant startTime = timeline.get(bestStartIndex).from();
        Instant endTime = timeline.get(bestStartIndex + requiredSlots - 1).to();
        boolean partialData = timeline.size() < expectedIntervals;

        return Optional.of(new ChargingWindow(startTime, endTime, roundToTwoDecimals(bestAverage), partialData));
    }

    private void validateDuration(int durationHours) {
        if (durationHours < MIN_DURATION_HOURS || durationHours > MAX_DURATION_HOURS) {
            throw new BookingValidationException(
                    "Charging duration must be between %d and %d hours".formatted(MIN_DURATION_HOURS, MAX_DURATION_HOURS)
            );
        }
    }

    private List<GenerationInterval> normalizeTimeline(List<GenerationInterval> intervals) {
        if (intervals == null || intervals.isEmpty()) {
            return List.of();
        }
        Map<Instant, GenerationInterval> deduplicated = new LinkedHashMap<>();
        intervals.stream()
                .sorted(Comparator.comparing(GenerationInterval::from))
                .forEach(interval -> deduplicated.putIfAbsent(interval.from(), interval));
        return List.copyOf(deduplicated.values());
    }

    private boolean isContiguousWindow(List<GenerationInterval> timeline, int startIndex, int requiredSlots) {
        for (int offset = 1; offset < requiredSlots; offset++) {
            GenerationInterval previous = timeline.get(startIndex + offset - 1);
            GenerationInterval current = timeline.get(startIndex + offset);
            if (!previous.isAdjacentBefore(current)) {
                return false;
            }
        }
        return true;
    }

    private double calculateWindowAverage(List<GenerationInterval> timeline, int startIndex, int requiredSlots) {
        double sum = 0.0;
        for (int offset = 0; offset < requiredSlots; offset++) {
            sum += timeline.get(startIndex + offset).cleanEnergyPercentage();
        }
        return sum / requiredSlots;
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
