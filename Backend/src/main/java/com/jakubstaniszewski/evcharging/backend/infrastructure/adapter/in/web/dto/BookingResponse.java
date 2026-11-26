package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * HTTP response representing a persisted charger reservation.
 *
 * @param id              booking identifier
 * @param chargerId       charger identifier
 * @param chargerName     charger name
 * @param chargerLocation charger physical location
 * @param userId          booking owner identifier
 * @param startTime       inclusive UTC start timestamp
 * @param endTime         exclusive UTC end timestamp
 * @param durationHours   reservation duration in whole hours
 * @param createdAt       UTC creation timestamp
 */
public record BookingResponse(
        UUID id,
        UUID chargerId,
        String chargerName,
        String chargerLocation,
        String userId,
        Instant startTime,
        Instant endTime,
        long durationHours,
        Instant createdAt
) {
}
