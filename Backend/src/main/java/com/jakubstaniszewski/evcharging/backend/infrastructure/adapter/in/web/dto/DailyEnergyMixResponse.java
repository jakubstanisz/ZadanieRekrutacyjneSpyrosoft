package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto;

import com.jakubstaniszewski.evcharging.backend.domain.model.energy.DataCompletenessStatus;

import java.time.LocalDate;
import java.util.Map;

/**
 * HTTP response summarizing a single UTC day's UK electricity generation mix.
 *
 * @param date                  UTC calendar date
 * @param averageFuelShares     average percentage share per fuel name (lowercase API identifiers)
 * @param cleanEnergyPercentage mean combined clean energy percentage
 * @param availableIntervals    number of 30-minute slots available
 * @param expectedIntervals     expected number of slots (48)
 * @param completenessStatus    completeness status ({@code COMPLETE}, {@code PARTIAL}, {@code UNAVAILABLE})
 */
public record DailyEnergyMixResponse(
        LocalDate date,
        Map<String, Double> averageFuelShares,
        double cleanEnergyPercentage,
        int availableIntervals,
        int expectedIntervals,
        DataCompletenessStatus completenessStatus
) {
}
