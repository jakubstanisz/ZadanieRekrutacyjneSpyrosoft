package com.jakubstaniszewski.evcharging.backend.application.service;

import com.jakubstaniszewski.evcharging.backend.application.dto.ChargingWindowResult;
import com.jakubstaniszewski.evcharging.backend.application.exception.ExternalEnergyDataUnavailableException;
import com.jakubstaniszewski.evcharging.backend.application.port.in.FindBestChargingWindowUseCase;
import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingValidationException;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.ChargingWindow;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.GenerationInterval;
import com.jakubstaniszewski.evcharging.backend.domain.port.GenerationMixProvider;
import com.jakubstaniszewski.evcharging.backend.domain.service.AvailableIntervalsMeanStrategy;
import com.jakubstaniszewski.evcharging.backend.domain.service.ChargingWindowOptimizer;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Application service coordinating forecast retrieval and optimal charging window computation
 * over the upcoming two-day UTC horizon (tomorrow and the day after tomorrow).
 */
@Service
public class ChargingWindowService implements FindBestChargingWindowUseCase {

    private static final int FORECAST_DAYS = 2;
    private static final int EXPECTED_FORECAST_INTERVALS =
            FORECAST_DAYS * AvailableIntervalsMeanStrategy.EXPECTED_INTERVALS_PER_DAY;

    private final GenerationMixProvider generationMixProvider;
    private final ChargingWindowOptimizer windowOptimizer;
    private final Clock clock;

    /**
     * Creates a new {@link ChargingWindowService}.
     *
     * @param generationMixProvider output port providing generation forecast intervals
     * @param windowOptimizer       domain service calculating the optimal window
     * @param clock                 UTC clock reference
     */
    public ChargingWindowService(
            GenerationMixProvider generationMixProvider,
            ChargingWindowOptimizer windowOptimizer,
            Clock clock
    ) {
        this.generationMixProvider = generationMixProvider;
        this.windowOptimizer = windowOptimizer;
        this.clock = clock;
    }

    @Override
    public ChargingWindowResult findBestWindow(int durationHours) {
        if (durationHours < ChargingWindowOptimizer.MIN_DURATION_HOURS
                || durationHours > ChargingWindowOptimizer.MAX_DURATION_HOURS) {
            throw new BookingValidationException(
                    "Charging duration must be between %d and %d hours"
                            .formatted(ChargingWindowOptimizer.MIN_DURATION_HOURS, ChargingWindowOptimizer.MAX_DURATION_HOURS)
            );
        }

        LocalDate todayUtc = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
        LocalDate tomorrowUtc = todayUtc.plusDays(1);
        Instant from = tomorrowUtc.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = tomorrowUtc.plusDays(FORECAST_DAYS).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<GenerationInterval> rawIntervals = generationMixProvider.fetchGenerationMix(from, to);
        List<GenerationInterval> forecastIntervals = filterToHorizon(rawIntervals, from, to);

        ChargingWindow bestWindow = windowOptimizer
                .findBestWindow(forecastIntervals, durationHours, EXPECTED_FORECAST_INTERVALS)
                .orElseThrow(() -> new ExternalEnergyDataUnavailableException(
                        "Insufficient contiguous forecast intervals to compute a %d-hour charging window"
                                .formatted(durationHours)
                ));

        return new ChargingWindowResult(
                bestWindow.startTime(),
                bestWindow.endTime(),
                bestWindow.cleanEnergyPercentage(),
                bestWindow.partialData()
        );
    }

    private List<GenerationInterval> filterToHorizon(
            List<GenerationInterval> intervals,
            Instant from,
            Instant to
    ) {
        if (intervals == null || intervals.isEmpty()) {
            return List.of();
        }
        return intervals.stream()
                .filter(interval -> !interval.from().isBefore(from) && interval.from().isBefore(to))
                .toList();
    }
}
