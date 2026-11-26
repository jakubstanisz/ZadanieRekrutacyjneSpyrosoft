package com.jakubstaniszewski.evcharging.backend.application.dto;

import java.util.UUID;

/**
 * Read-only view of an EV charger returned by application queries.
 *
 * @param id            unique charger identifier
 * @param name          charger name or code
 * @param location      physical location
 * @param connectorType connector standard
 * @param powerKw       maximum charging power in kilowatts
 */
public record ChargerResult(
        UUID id,
        String name,
        String location,
        String connectorType,
        int powerKw
) {
}
