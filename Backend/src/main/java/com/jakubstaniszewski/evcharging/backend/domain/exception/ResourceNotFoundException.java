package com.jakubstaniszewski.evcharging.backend.domain.exception;

/**
 * Thrown when a requested domain aggregate or entity (such as a charger or booking) cannot be found.
 */
public class ResourceNotFoundException extends DomainException {

    /**
     * Creates a new resource-not-found exception.
     *
     * @param message description of the missing resource
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
