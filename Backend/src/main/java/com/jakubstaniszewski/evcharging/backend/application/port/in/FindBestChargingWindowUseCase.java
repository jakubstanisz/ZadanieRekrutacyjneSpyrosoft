package com.jakubstaniszewski.evcharging.backend.application.port.in;

import com.jakubstaniszewski.evcharging.backend.application.dto.ChargingWindowResult;

/**
 * Input port for locating the optimal clean-energy charging window across the two-day forecast.
 */
public interface FindBestChargingWindowUseCase {

    /**
     * Finds the continuous charging window of {@code durationHours} with the highest average
     * clean energy percentage across tomorrow and the day after tomorrow (UTC).
     *
     * @param durationHours charging duration in whole hours (1–6)
     * @return optimal charging window
     */
    ChargingWindowResult findBestWindow(int durationHours);
}
