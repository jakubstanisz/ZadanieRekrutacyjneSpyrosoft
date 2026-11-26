package com.jakubstaniszewski.evcharging.backend.application.port.in;

import com.jakubstaniszewski.evcharging.backend.application.dto.ChargerBookedSlotResult;

import java.util.List;
import java.util.UUID;

/**
 * Input port for retrieving anonymized booked time slots for a specific charger.
 */
public interface GetChargerScheduleUseCase {

    /**
     * Returns all booked time slots for the given charger so calendar views can show availability.
     *
     * @param chargerId target charger identifier
     * @return list of booked time intervals
     */
    List<ChargerBookedSlotResult> getChargerBookedSlots(UUID chargerId);
}
