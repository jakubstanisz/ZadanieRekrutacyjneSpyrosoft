package com.jakubstaniszewski.evcharging.backend.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Command carrying inputs required to create a new charger booking.
 *
 * @param chargerId target charger identifier
 * @param userId    authenticated or header-provided user identifier
 * @param startTime inclusive UTC start timestamp
 * @param endTime   exclusive UTC end timestamp
 */
public record CreateBookingCommand(
        UUID chargerId,
        String userId,
        Instant startTime,
        Instant endTime
) {
}
