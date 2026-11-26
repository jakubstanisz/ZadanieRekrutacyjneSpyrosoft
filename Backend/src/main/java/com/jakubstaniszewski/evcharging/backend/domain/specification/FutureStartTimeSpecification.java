package com.jakubstaniszewski.evcharging.backend.domain.specification;

import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingValidationException;

/**
 * Ensures that a booking does not start in the past relative to the current UTC timestamp.
 */
public final class FutureStartTimeSpecification implements BookingSpecification {

    @Override
    public void validate(BookingCandidate candidate) {
        if (candidate.timeRange().startTime().isBefore(candidate.currentTime())) {
            throw new BookingValidationException("Booking start time must not be in the past");
        }
    }
}
