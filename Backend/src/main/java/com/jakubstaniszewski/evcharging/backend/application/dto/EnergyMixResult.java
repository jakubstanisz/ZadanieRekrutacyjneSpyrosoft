package com.jakubstaniszewski.evcharging.backend.application.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Result of querying the UK generation mix across multiple upcoming days.
 *
 * @param days         ordered daily energy mix summaries
 * @param cleanestDate date with the highest clean energy percentage among available days, or {@code null}
 */
public record EnergyMixResult(
        List<DailyEnergyMixResult> days,
        LocalDate cleanestDate
) {
}
