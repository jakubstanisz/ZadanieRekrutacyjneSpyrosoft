package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto;

import java.time.Instant;

/**
 * HTTP response representing an anonymized booked time slot on a charger for calendar availability display.
 *
 * @param startTime inclusive UTC start timestamp
 * @param endTime   exclusive UTC end timestamp
 */
public record ChargerBookedSlotResponse(
        Instant startTime,
        Instant endTime
) {
}
