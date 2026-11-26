package com.jakubstaniszewski.evcharging.backend.domain.specification;

import com.jakubstaniszewski.evcharging.backend.domain.model.booking.Booking;
import com.jakubstaniszewski.evcharging.backend.domain.model.booking.TimeRange;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Parameter object carrying all data required to validate a proposed booking against domain specifications.
 *
 * @param chargerId               target charger identifier
 * @param userId                  requesting user identifier
 * @param timeRange               proposed reservation time interval
 * @param currentTime             reference UTC timestamp ("now")
 * @param existingChargerBookings existing bookings on the same charger
 */
public record BookingCandidate(
        UUID chargerId,
        String userId,
        TimeRange timeRange,
        Instant currentTime,
        List<Booking> existingChargerBookings
) {

    /**
     * Validates mandatory fields and makes an immutable copy of existing bookings.
     */
    public BookingCandidate {
        Objects.requireNonNull(chargerId, "Charger id must not be null");
        Objects.requireNonNull(timeRange, "Time range must not be null");
        Objects.requireNonNull(currentTime, "Current time must not be null");
        existingChargerBookings = existingChargerBookings == null ? List.of() : List.copyOf(existingChargerBookings);
    }
}
