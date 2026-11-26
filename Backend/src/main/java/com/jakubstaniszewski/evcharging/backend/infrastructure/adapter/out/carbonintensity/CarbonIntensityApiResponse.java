package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.carbonintensity;

import java.util.List;

/**
 * Root JSON response wrapper returned by the UK Carbon Intensity generation endpoint.
 *
 * @param data list of 30-minute generation mix slots
 */
public record CarbonIntensityApiResponse(
        List<GenerationSlotResponse> data
) {
}
