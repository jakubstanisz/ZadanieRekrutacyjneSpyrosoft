package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * HTTP response containing daily generation mix summaries and the cleanest day indicator.
 *
 * @param days         ordered daily energy mix summaries
 * @param cleanestDate UTC date with the highest clean energy percentage, or {@code null}
 */
public record EnergyMixResponse(
        List<DailyEnergyMixResponse> days,
        LocalDate cleanestDate
) {
}
