package com.jakubstaniszewski.evcharging.backend.domain.specification;

import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingValidationException;

/**
 * Ensures that a booking duration is an exact number of whole hours between 1 and 6 inclusive.
 */
public final class BookingDurationSpecification implements BookingSpecification {

    public static final int MIN_DURATION_HOURS = 1;
    public static final int MAX_DURATION_HOURS = 6;

    @Override
    public void validate(BookingCandidate candidate) {
        if (!candidate.timeRange().isWholeHoursBetween(MIN_DURATION_HOURS, MAX_DURATION_HOURS)) {
            throw new BookingValidationException(
                    "Booking duration must be a whole number of hours between %d and %d"
                            .formatted(MIN_DURATION_HOURS, MAX_DURATION_HOURS)
            );
        }
    }
}
