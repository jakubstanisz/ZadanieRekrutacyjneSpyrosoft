package com.jakubstaniszewski.evcharging.backend.domain.model.energy;

/**
 * Describes the completeness of generation mix intervals available for a single UTC day.
 */
public enum DataCompletenessStatus {
    /** Coverage meets or exceeds the required threshold (90% of expected 30-minute intervals). */
    COMPLETE,
    /** Some intervals are available, but coverage is below the 90% completeness threshold. */
    PARTIAL,
    /** No intervals were returned for the requested day. */
    UNAVAILABLE
}
