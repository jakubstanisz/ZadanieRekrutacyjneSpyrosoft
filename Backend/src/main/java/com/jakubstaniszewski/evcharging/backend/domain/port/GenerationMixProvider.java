package com.jakubstaniszewski.evcharging.backend.domain.port;

import com.jakubstaniszewski.evcharging.backend.domain.model.energy.GenerationInterval;

import java.time.Instant;
import java.util.List;

/**
 * Output port for retrieving UK electricity generation mix intervals for a UTC time window.
 */
public interface GenerationMixProvider {

    /**
     * Fetches 30-minute generation mix intervals overlapping or falling within {@code [from, to)}.
     *
     * @param from inclusive UTC start timestamp
     * @param to   exclusive UTC end timestamp
     * @return list of available {@link GenerationInterval} entries (may be partial)
     */
    List<GenerationInterval> fetchGenerationMix(Instant from, Instant to);
}
