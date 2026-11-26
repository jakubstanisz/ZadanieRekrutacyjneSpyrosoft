package com.jakubstaniszewski.evcharging.backend.application.service;

import com.jakubstaniszewski.evcharging.backend.application.dto.EnergyMixResult;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.DataCompletenessStatus;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.EnergySource;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.GenerationInterval;
import com.jakubstaniszewski.evcharging.backend.domain.port.GenerationMixProvider;
import com.jakubstaniszewski.evcharging.backend.domain.service.AvailableIntervalsMeanStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EnergyMixService} verifying UTC day grouping, boundary filtering, and partial-data tolerance.
 */
@ExtendWith(MockitoExtension.class)
class EnergyMixServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-12-01T10:00:00Z");

    @Mock
    private GenerationMixProvider generationMixProvider;

    private EnergyMixService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        service = new EnergyMixService(generationMixProvider, new AvailableIntervalsMeanStrategy(), clock);
    }

    @Test
    void shouldGroupByUtcDayExcludePreviousDayBoundaryAndPickCleanestDate() {
        Instant from = Instant.parse("2026-12-01T00:00:00Z");
        Instant to = Instant.parse("2026-12-04T00:00:00Z");

        GenerationInterval yesterdayTrailing = slot("2026-11-30T23:30:00Z", 99.0);
        GenerationInterval day1Slot = slot("2026-12-01T00:00:00Z", 40.0);
        GenerationInterval day2Slot = slot("2026-12-02T12:00:00Z", 85.0);

        when(generationMixProvider.fetchGenerationMix(from, to))
                .thenReturn(List.of(yesterdayTrailing, day1Slot, day2Slot));

        EnergyMixResult result = service.getEnergyMix(3);

        assertThat(result.days()).hasSize(3);
        assertThat(result.days().get(0).cleanEnergyPercentage()).isEqualTo(40.0);
        assertThat(result.days().get(0).completenessStatus()).isEqualTo(DataCompletenessStatus.PARTIAL);
        assertThat(result.days().get(1).cleanEnergyPercentage()).isEqualTo(85.0);
        assertThat(result.days().get(2).completenessStatus()).isEqualTo(DataCompletenessStatus.UNAVAILABLE);
        assertThat(result.cleanestDate()).isEqualTo(LocalDate.of(2026, 12, 2));
    }

    private GenerationInterval slot(String startIso, double windShare) {
        Instant start = Instant.parse(startIso);
        return new GenerationInterval(
                start,
                start.plusSeconds(1800),
                Map.of(EnergySource.WIND, windShare, EnergySource.GAS, 100.0 - windShare)
        );
    }
}
