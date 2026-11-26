package com.jakubstaniszewski.evcharging.backend.domain.exception;

/**
 * Thrown when a user attempts to perform an operation on a resource they do not own.
 */
public class ForbiddenActionException extends DomainException {

    /**
     * Creates a new forbidden action exception.
     *
     * @param message description of the unauthorized action
     */
    public ForbiddenActionException(String message) {
        super(message);
    }
}
