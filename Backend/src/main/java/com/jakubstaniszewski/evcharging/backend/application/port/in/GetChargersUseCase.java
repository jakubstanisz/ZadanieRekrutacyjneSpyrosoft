package com.jakubstaniszewski.evcharging.backend.application.port.in;

import com.jakubstaniszewski.evcharging.backend.application.dto.ChargerResult;

import java.util.List;

/**
 * Input port for listing all available EV chargers.
 */
public interface GetChargersUseCase {

    /**
     * Returns all EV chargers configured in the system.
     *
     * @return list of chargers
     */
    List<ChargerResult> getChargers();
}
