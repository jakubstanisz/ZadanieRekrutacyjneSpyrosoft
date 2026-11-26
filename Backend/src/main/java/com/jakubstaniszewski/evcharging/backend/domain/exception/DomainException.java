package com.jakubstaniszewski.evcharging.backend.domain.exception;

/**
 * Base class for all business rule and domain invariant violations.
 */
public abstract class DomainException extends RuntimeException {

    /**
     * Creates a new domain exception with a descriptive message.
     *
     * @param message explanation of the violated domain rule
     */
    protected DomainException(String message) {
        super(message);
    }
}
