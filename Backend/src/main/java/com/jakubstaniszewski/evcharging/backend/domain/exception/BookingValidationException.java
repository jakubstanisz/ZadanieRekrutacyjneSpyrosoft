package com.jakubstaniszewski.evcharging.backend.domain.exception;

/**
 * Thrown when booking or optimization parameters violate domain validation rules
 * (e.g., invalid time range, start time in the past, or duration outside allowed bounds).
 */
public class BookingValidationException extends DomainException {

    /**
     * Creates a new validation exception.
     *
     * @param message description of the validation failure
     */
    public BookingValidationException(String message) {
        super(message);
    }
}
