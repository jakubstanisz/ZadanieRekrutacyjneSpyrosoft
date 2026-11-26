package com.jakubstaniszewski.evcharging.backend.application.port.in;

import com.jakubstaniszewski.evcharging.backend.application.dto.EnergyMixResult;

/**
 * Input port for retrieving daily UK electricity generation mix summaries.
 */
public interface GetEnergyMixUseCase {

    /**
     * Computes daily energy mix summaries starting from today (UTC) for the requested number of days.
     *
     * @param days number of days to summarize (1–3)
     * @return aggregated daily summaries and the cleanest available day
     */
    EnergyMixResult getEnergyMix(int days);
}
