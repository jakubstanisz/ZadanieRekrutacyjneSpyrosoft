package com.jakubstaniszewski.evcharging.backend.domain.service;

import com.jakubstaniszewski.evcharging.backend.domain.model.energy.DailyEnergyMix;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.GenerationInterval;

import java.time.LocalDate;
import java.util.List;

/**
 * Strategy contract for aggregating 30-minute generation intervals into a daily energy mix summary,
 * including handling of incomplete or missing forecast data.
 */
public interface CleanEnergyCalculationStrategy {

    /**
     * Calculates the daily energy mix summary for a given UTC date.
     *
     * @param date              target UTC calendar date
     * @param intervals         intervals belonging to that date
     * @param expectedIntervals expected number of intervals in a full day (typically 48)
     * @return aggregated {@link DailyEnergyMix}
     */
    DailyEnergyMix calculateDailyMix(LocalDate date, List<GenerationInterval> intervals, int expectedIntervals);
}
