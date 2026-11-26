package com.jakubstaniszewski.evcharging.backend.domain.exception;

/**
 * Thrown when a requested charger reservation overlaps with an existing booking.
 */
public class BookingConflictException extends DomainException {

    /**
     * Creates a new booking conflict exception.
     *
     * @param message description of the scheduling conflict
     */
    public BookingConflictException(String message) {
        super(message);
    }
}
