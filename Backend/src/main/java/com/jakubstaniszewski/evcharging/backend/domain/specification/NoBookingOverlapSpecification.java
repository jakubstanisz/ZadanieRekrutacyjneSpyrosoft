package com.jakubstaniszewski.evcharging.backend.domain.specification;

import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingConflictException;

/**
 * Ensures that a proposed booking does not overlap with any existing reservation on the same charger.
 */
public final class NoBookingOverlapSpecification implements BookingSpecification {

    @Override
    public void validate(BookingCandidate candidate) {
        boolean hasOverlap = candidate.existingChargerBookings().stream()
                .anyMatch(existing -> existing.overlaps(candidate.timeRange()));
        if (hasOverlap) {
            throw new BookingConflictException(
                    "Charger %s is already booked during the requested time range".formatted(candidate.chargerId())
            );
        }
    }
}
