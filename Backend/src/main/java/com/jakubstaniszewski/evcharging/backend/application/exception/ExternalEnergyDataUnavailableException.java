package com.jakubstaniszewski.evcharging.backend.application.exception;

/**
 * Thrown when external energy generation data cannot be retrieved or does not contain
 * sufficient contiguous intervals to fulfill a request.
 */
public class ExternalEnergyDataUnavailableException extends RuntimeException {

    /**
     * Creates an exception with an explanatory message.
     *
     * @param message explanation of the energy data unavailability
     */
    public ExternalEnergyDataUnavailableException(String message) {
        super(message);
    }

    /**
     * Creates an exception wrapping the underlying infrastructure failure.
     *
     * @param message explanation of the energy data unavailability
     * @param cause   root cause exception
     */
    public ExternalEnergyDataUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
