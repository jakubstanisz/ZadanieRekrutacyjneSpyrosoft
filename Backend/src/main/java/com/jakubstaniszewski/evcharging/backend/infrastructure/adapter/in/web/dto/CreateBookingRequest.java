package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * HTTP request body for creating a new EV charger booking.
 *
 * @param chargerId target charger identifier
 * @param startTime inclusive UTC booking start timestamp (ISO-8601)
 * @param endTime   exclusive UTC booking end timestamp (ISO-8601)
 */
public record CreateBookingRequest(
        @NotNull(message = "chargerId is required")
        UUID chargerId,

        @NotNull(message = "startTime is required")
        Instant startTime,

        @NotNull(message = "endTime is required")
        Instant endTime
) {
}
