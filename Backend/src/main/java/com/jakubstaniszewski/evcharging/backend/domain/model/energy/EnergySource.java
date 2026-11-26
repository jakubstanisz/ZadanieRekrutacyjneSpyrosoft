package com.jakubstaniszewski.evcharging.backend.domain.model.energy;

import java.util.Arrays;
import java.util.Locale;

/**
 * Represents UK electricity generation sources reported by the Carbon Intensity API.
 * Clean energy classification is encapsulated directly on each source to satisfy the Open/Closed Principle.
 */
public enum EnergySource {

    BIOMASS("biomass", true),
    NUCLEAR("nuclear", true),
    HYDRO("hydro", true),
    WIND("wind", true),
    SOLAR("solar", true),
    GAS("gas", false),
    COAL("coal", false),
    IMPORTS("imports", false),
    OTHER("other", false);

    private final String apiName;
    private final boolean clean;

    EnergySource(String apiName, boolean clean) {
        this.apiName = apiName;
        this.clean = clean;
    }

    /**
     * Returns the identifier used by the external Carbon Intensity API.
     *
     * @return lowercase fuel identifier
     */
    public String apiName() {
        return apiName;
    }

    /**
     * Indicates whether this energy source is classified as clean energy.
     *
     * @return {@code true} for biomass, nuclear, hydro, wind, and solar; {@code false} otherwise
     */
    public boolean isClean() {
        return clean;
    }

    /**
     * Maps a raw fuel name from the external API to a domain {@link EnergySource}.
     * Unknown labels gracefully fall back to {@link #OTHER}.
     *
     * @param rawFuelName raw fuel name from the API
     * @return matching {@link EnergySource}, or {@link #OTHER} if unrecognized
     */
    public static EnergySource fromApiName(String rawFuelName) {
        if (rawFuelName == null || rawFuelName.isBlank()) {
            return OTHER;
        }
        String normalized = rawFuelName.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(source -> source.apiName.equals(normalized))
                .findFirst()
                .orElse(OTHER);
    }
}
