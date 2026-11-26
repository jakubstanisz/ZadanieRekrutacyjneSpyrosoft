package com.jakubstaniszewski.evcharging.backend.domain.service;

import com.jakubstaniszewski.evcharging.backend.domain.model.energy.DailyEnergyMix;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.DataCompletenessStatus;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.EnergySource;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.GenerationInterval;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AvailableIntervalsMeanStrategy} covering clean-energy sums and partial-data thresholds.
 */
class AvailableIntervalsMeanStrategyTest {

    private final AvailableIntervalsMeanStrategy strategy = new AvailableIntervalsMeanStrategy();
    private final LocalDate date = LocalDate.of(2026, 12, 1);

    @Test
    void shouldCalculateCompleteDayCleanEnergyAverage() {
        List<GenerationInterval> intervals = createIntervals(48, 30.0, 20.0, 50.0);

        DailyEnergyMix result = strategy.calculateDailyMix(date, intervals, 48);

        assertThat(result.completenessStatus()).isEqualTo(DataCompletenessStatus.COMPLETE);
        assertThat(result.availableIntervals()).isEqualTo(48);
        assertThat(result.cleanEnergyPercentage()).isEqualTo(50.0);
        assertThat(result.averageFuelShares().get(EnergySource.WIND)).isEqualTo(30.0);
        assertThat(result.averageFuelShares().get(EnergySource.SOLAR)).isEqualTo(20.0);
        assertThat(result.averageFuelShares().get(EnergySource.GAS)).isEqualTo(50.0);
    }

    @Test
    void shouldMarkDayCompleteAtNinetyPercentThresholdAndPartialBelowIt() {
        List<GenerationInterval> fortyFourSlots = createIntervals(44, 40.0, 10.0, 50.0);
        List<GenerationInterval> fortyThreeSlots = createIntervals(43, 40.0, 10.0, 50.0);

        DailyEnergyMix atThreshold = strategy.calculateDailyMix(date, fortyFourSlots, 48);
        DailyEnergyMix belowThreshold = strategy.calculateDailyMix(date, fortyThreeSlots, 48);

        assertThat(atThreshold.completenessStatus()).isEqualTo(DataCompletenessStatus.COMPLETE);
        assertThat(belowThreshold.completenessStatus()).isEqualTo(DataCompletenessStatus.PARTIAL);
        assertThat(belowThreshold.cleanEnergyPercentage()).isEqualTo(50.0);
    }

    @Test
    void shouldReturnUnavailableWithZeroSharesWhenNoIntervalsExist() {
        DailyEnergyMix result = strategy.calculateDailyMix(date, List.of(), 48);

        assertThat(result.completenessStatus()).isEqualTo(DataCompletenessStatus.UNAVAILABLE);
        assertThat(result.availableIntervals()).isZero();
        assertThat(result.cleanEnergyPercentage()).isZero();
        assertThat(result.averageFuelShares().values()).containsOnly(0.0);
    }

    private List<GenerationInterval> createIntervals(int count, double wind, double solar, double gas) {
        Instant startOfDay = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Instant from = startOfDay.plusSeconds(i * 1800L);
                    return new GenerationInterval(
                            from,
                            from.plusSeconds(1800L),
                            Map.of(
                                    EnergySource.WIND, wind,
                                    EnergySource.SOLAR, solar,
                                    EnergySource.GAS, gas
                            )
                    );
                })
                .toList();
    }
}
