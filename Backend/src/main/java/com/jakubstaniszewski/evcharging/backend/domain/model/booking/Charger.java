package com.jakubstaniszewski.evcharging.backend.domain.model.booking;

import java.util.Objects;
import java.util.UUID;

/**
 * Domain model representing a physical EV charging station available for booking.
 *
 * @param id            unique charger identifier
 * @param name          human-readable charger code or name
 * @param location      physical address or bay description
 * @param connectorType connector standard (e.g., Type 2, CCS2)
 * @param powerKw       maximum charging power in kilowatts
 */
public record Charger(
        UUID id,
        String name,
        String location,
        String connectorType,
        int powerKw
) {

    /**
     * Validates charger invariants upon instantiation.
     */
    public Charger {
        Objects.requireNonNull(id, "Charger id must not be null");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Charger name must not be blank");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Charger location must not be blank");
        }
        if (connectorType == null || connectorType.isBlank()) {
            throw new IllegalArgumentException("Connector type must not be blank");
        }
        if (powerKw <= 0) {
            throw new IllegalArgumentException("Charger power must be positive");
        }
    }
}
