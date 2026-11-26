package com.jakubstaniszewski.evcharging.backend.application.service;

import com.jakubstaniszewski.evcharging.backend.application.dto.DailyEnergyMixResult;
import com.jakubstaniszewski.evcharging.backend.application.dto.EnergyMixResult;
import com.jakubstaniszewski.evcharging.backend.application.port.in.GetEnergyMixUseCase;
import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingValidationException;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.DailyEnergyMix;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.DataCompletenessStatus;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.GenerationInterval;
import com.jakubstaniszewski.evcharging.backend.domain.port.GenerationMixProvider;
import com.jakubstaniszewski.evcharging.backend.domain.service.AvailableIntervalsMeanStrategy;
import com.jakubstaniszewski.evcharging.backend.domain.service.CleanEnergyCalculationStrategy;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Application service orchestrating retrieval and daily aggregation of UK electricity generation mix data.
 */
@Service
public class EnergyMixService implements GetEnergyMixUseCase {

    private static final int MIN_DAYS = 1;
    private static final int MAX_DAYS = 3;

    private final GenerationMixProvider generationMixProvider;
    private final CleanEnergyCalculationStrategy calculationStrategy;
    private final Clock clock;

    /**
     * Creates a new {@link EnergyMixService}.
     *
     * @param generationMixProvider output port providing generation intervals
     * @param calculationStrategy   strategy for aggregating daily mix and completeness
     * @param clock                 UTC clock reference
     */
    public EnergyMixService(
            GenerationMixProvider generationMixProvider,
            CleanEnergyCalculationStrategy calculationStrategy,
            Clock clock
    ) {
        this.generationMixProvider = generationMixProvider;
        this.calculationStrategy = calculationStrategy;
        this.clock = clock;
    }

    @Override
    public EnergyMixResult getEnergyMix(int days) {
        if (days < MIN_DAYS || days > MAX_DAYS) {
            throw new BookingValidationException(
                    "Days parameter must be between %d and %d".formatted(MIN_DAYS, MAX_DAYS)
            );
        }

        LocalDate todayUtc = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
        Instant from = todayUtc.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = todayUtc.plusDays(days).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<GenerationInterval> intervals = generationMixProvider.fetchGenerationMix(from, to);
        Map<LocalDate, List<GenerationInterval>> intervalsByDate = groupByUtcDate(intervals, from, to);

        List<DailyEnergyMixResult> dailyResults = IntStream.range(0, days)
                .mapToObj(todayUtc::plusDays)
                .map(date -> calculateForDate(date, intervalsByDate.getOrDefault(date, List.of())))
                .toList();

        LocalDate cleanestDate = resolveCleanestDate(dailyResults);
        return new EnergyMixResult(dailyResults, cleanestDate);
    }

    private Map<LocalDate, List<GenerationInterval>> groupByUtcDate(
            List<GenerationInterval> intervals,
            Instant from,
            Instant to
    ) {
        if (intervals == null || intervals.isEmpty()) {
            return Map.of();
        }
        Map<Instant, GenerationInterval> uniqueByStart = new LinkedHashMap<>();
        intervals.stream()
                .filter(interval -> !interval.from().isBefore(from) && interval.from().isBefore(to))
                .sorted(Comparator.comparing(GenerationInterval::from))
                .forEach(interval -> uniqueByStart.putIfAbsent(interval.from(), interval));

        return uniqueByStart.values().stream()
                .collect(Collectors.groupingBy(
                        interval -> LocalDate.ofInstant(interval.from(), ZoneOffset.UTC)
                ));
    }

    private DailyEnergyMixResult calculateForDate(LocalDate date, List<GenerationInterval> dayIntervals) {
        DailyEnergyMix dailyMix = calculationStrategy.calculateDailyMix(
                date,
                dayIntervals,
                AvailableIntervalsMeanStrategy.EXPECTED_INTERVALS_PER_DAY
        );
        return new DailyEnergyMixResult(
                dailyMix.date(),
                dailyMix.averageFuelShares(),
                dailyMix.cleanEnergyPercentage(),
                dailyMix.availableIntervals(),
                dailyMix.expectedIntervals(),
                dailyMix.completenessStatus()
        );
    }

    private LocalDate resolveCleanestDate(List<DailyEnergyMixResult> dailyResults) {
        return dailyResults.stream()
                .filter(day -> day.completenessStatus() != DataCompletenessStatus.UNAVAILABLE)
                .max(Comparator.comparingDouble(DailyEnergyMixResult::cleanEnergyPercentage))
                .map(DailyEnergyMixResult::date)
                .orElse(null);
    }
}
