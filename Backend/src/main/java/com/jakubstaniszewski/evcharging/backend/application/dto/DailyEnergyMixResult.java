package com.jakubstaniszewski.evcharging.backend.application.dto;

import com.jakubstaniszewski.evcharging.backend.domain.model.energy.DataCompletenessStatus;
import com.jakubstaniszewski.evcharging.backend.domain.model.energy.EnergySource;

import java.time.LocalDate;
import java.util.Map;

/**
 * Application-level summary of UK generation mix for a single UTC day.
 *
 * @param date                  UTC calendar date
 * @param averageFuelShares     mean percentage share per energy source
 * @param cleanEnergyPercentage mean total clean energy percentage
 * @param availableIntervals    number of 30-minute intervals available
 * @param expectedIntervals     expected number of 30-minute intervals (48)
 * @param completenessStatus    completeness indicator ({@code COMPLETE}, {@code PARTIAL}, {@code UNAVAILABLE})
 */
public record DailyEnergyMixResult(
        LocalDate date,
        Map<EnergySource, Double> averageFuelShares,
        double cleanEnergyPercentage,
        int availableIntervals,
        int expectedIntervals,
        DataCompletenessStatus completenessStatus
) {
}
