package com.jakubstaniszewski.evcharging.backend.application.dto;

import java.time.Instant;

/**
 * Anonymized booked time interval for a specific charger, used by calendar availability views.
 *
 * @param startTime inclusive UTC start timestamp
 * @param endTime   exclusive UTC end timestamp
 */
public record ChargerBookedSlotResult(
        Instant startTime,
        Instant endTime
) {
}
