package com.jakubstaniszewski.evcharging.backend.domain.service;

import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingValidationException;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.ChargingWindow;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.EnergySource;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.GenerationInterval;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ChargingWindowOptimizer} covering midnight crossing, tie-breaking, and gaps.
 */
class ChargingWindowOptimizerTest {

    private final ChargingWindowOptimizer optimizer = new ChargingWindowOptimizer();

    @Test
    void shouldFindBestWindowCrossingMidnight() {
        Instant base = Instant.parse("2026-12-01T22:30:00Z");
        // Slots: 22:30 (20%), 23:00 (80%), 23:30 (85%), 00:00 (90%), 00:30 (85%), 01:00 (30%)
        // Best 2-hour (4-slot) window: 23:00 -> 01:00 with average (80+85+90+85)/4 = 85.0%
        List<GenerationInterval> intervals = List.of(
                slot(base, 20.0),
                slot(base.plusSeconds(1800), 80.0),
                slot(base.plusSeconds(3600), 85.0),
                slot(base.plusSeconds(5400), 90.0),
                slot(base.plusSeconds(7200), 85.0),
                slot(base.plusSeconds(9000), 30.0)
        );

        Optional<ChargingWindow> result = optimizer.findBestWindow(intervals, 2, 6);

        assertThat(result).isPresent();
        assertThat(result.get().startTime()).isEqualTo(Instant.parse("2026-12-01T23:00:00Z"));
        assertThat(result.get().endTime()).isEqualTo(Instant.parse("2026-12-02T01:00:00Z"));
        assertThat(result.get().cleanEnergyPercentage()).isEqualTo(85.0);
        assertThat(result.get().partialData()).isFalse();
    }

    @Test
    void shouldSelectEarliestWindowOnExactTie() {
        Instant base = Instant.parse("2026-12-02T08:00:00Z");
        // Window 1: 08:00-09:00 avg 70%, Window 2: 10:00-11:00 avg 70%
        List<GenerationInterval> intervals = List.of(
                slot(base, 70.0),
                slot(base.plusSeconds(1800), 70.0),
                slot(base.plusSeconds(3600), 30.0),
                slot(base.plusSeconds(5400), 30.0),
                slot(base.plusSeconds(7200), 70.0),
                slot(base.plusSeconds(9000), 70.0)
        );

        Optional<ChargingWindow> result = optimizer.findBestWindow(intervals, 1, 96);

        assertThat(result).isPresent();
        assertThat(result.get().startTime()).isEqualTo(Instant.parse("2026-12-02T08:00:00Z"));
        assertThat(result.get().endTime()).isEqualTo(Instant.parse("2026-12-02T09:00:00Z"));
        assertThat(result.get().partialData()).isTrue();
    }

    @Test
    void shouldSkipNonContiguousSlotsAndReturnEmptyWhenNoWindowFits() {
        Instant base = Instant.parse("2026-12-02T08:00:00Z");
        // Gap between 08:30 and 09:30 prevents forming any 1-hour (2-slot) contiguous window
        List<GenerationInterval> fragmented = List.of(
                slot(base, 90.0),
                slot(base.plusSeconds(5400), 95.0)
        );

        Optional<ChargingWindow> result = optimizer.findBestWindow(fragmented, 1, 96);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRejectInvalidChargingDuration() {
        assertThatThrownBy(() -> optimizer.findBestWindow(List.of(), 0, 96))
                .isInstanceOf(BookingValidationException.class);
        assertThatThrownBy(() -> optimizer.findBestWindow(List.of(), 7, 96))
                .isInstanceOf(BookingValidationException.class);
    }

    private GenerationInterval slot(Instant from, double windShare) {
        return new GenerationInterval(
                from,
                from.plusSeconds(1800),
                Map.of(EnergySource.WIND, windShare, EnergySource.GAS, 100.0 - windShare)
        );
    }
}
