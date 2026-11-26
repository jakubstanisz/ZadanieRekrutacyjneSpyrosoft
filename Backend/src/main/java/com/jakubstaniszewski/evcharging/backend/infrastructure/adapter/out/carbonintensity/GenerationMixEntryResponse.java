package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.carbonintensity;

/**
 * JSON DTO representing a single fuel share entry inside a generation slot.
 *
 * @param fuel fuel identifier (e.g., {@code wind}, {@code solar}, {@code gas})
 * @param perc percentage share in the interval
 */
public record GenerationMixEntryResponse(
        String fuel,
        double perc
) {
}
