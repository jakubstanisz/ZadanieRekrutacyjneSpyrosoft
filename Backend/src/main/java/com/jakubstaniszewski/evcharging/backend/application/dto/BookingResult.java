package com.jakubstaniszewski.evcharging.backend.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Application-level representation of a charger booking enriched with charger metadata.
 *
 * @param id              booking identifier
 * @param chargerId       charger identifier
 * @param chargerName     charger name
 * @param chargerLocation charger physical location
 * @param userId          booking owner identifier
 * @param startTime       inclusive UTC start timestamp
 * @param endTime         exclusive UTC end timestamp
 * @param durationHours   duration in whole hours
 * @param createdAt       UTC timestamp when booking was created
 */
public record BookingResult(
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
