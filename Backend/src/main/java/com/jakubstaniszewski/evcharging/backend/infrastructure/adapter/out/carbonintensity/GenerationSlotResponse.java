package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.carbonintensity;

import java.util.List;

/**
 * JSON DTO representing a single 30-minute generation mix slot from the Carbon Intensity API.
 *
 * @param from          UTC start timestamp string (e.g., {@code 2025-01-15T00:00Z})
 * @param to            UTC end timestamp string
 * @param generationmix percentage breakdown by fuel type
 */
public record GenerationSlotResponse(
        String from,
        String to,
        List<GenerationMixEntryResponse> generationmix
) {
}
