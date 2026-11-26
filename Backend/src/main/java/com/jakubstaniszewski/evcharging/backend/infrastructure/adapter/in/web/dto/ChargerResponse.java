package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.in.web.dto;

import java.util.UUID;

/**
 * HTTP response representing an available EV charger.
 *
 * @param id            unique charger identifier
 * @param name          charger name or designation
 * @param location      physical address or bay description
 * @param connectorType connector standard
 * @param powerKw       maximum charging power in kilowatts
 */
public record ChargerResponse(
        UUID id,
        String name,
        String location,
        String connectorType,
        int powerKw
) {
}
