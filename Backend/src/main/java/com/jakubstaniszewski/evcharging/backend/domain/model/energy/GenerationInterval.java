package com.jakubstaniszewski.evcharging.backend.domain.model.energy;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a single 30-minute electricity generation mix interval in UTC.
 *
 * @param from       inclusive start timestamp of the interval
 * @param to         exclusive end timestamp of the interval
 * @param fuelShares percentage share per {@link EnergySource} within this interval
 */
public record GenerationInterval(
        Instant from,
        Instant to,
        Map<EnergySource, Double> fuelShares
) {

    /**
     * Validates timestamps and creates an immutable defensive copy of the fuel share map.
     */
    public GenerationInterval {
        Objects.requireNonNull(from, "Interval start timestamp must not be null");
        Objects.requireNonNull(to, "Interval end timestamp must not be null");
        Objects.requireNonNull(fuelShares, "Fuel shares map must not be null");
        if (!to.isAfter(from)) {
            throw new IllegalArgumentException("Interval end timestamp must be after start timestamp");
        }
        fuelShares = Map.copyOf(fuelShares);
    }

    /**
     * Calculates the total percentage share of all clean energy sources in this interval.
     *
     * @return sum of clean energy source percentages
     */
    public double cleanEnergyPercentage() {
        return fuelShares.entrySet().stream()
                .filter(entry -> entry.getKey().isClean())
                .mapToDouble(Map.Entry::getValue)
                .sum();
    }

    /**
     * Returns the percentage share of a specific source, or {@code 0.0} if absent.
     *
     * @param source energy source to look up
     * @return percentage share in this interval
     */
    public double shareOf(EnergySource source) {
        return fuelShares.getOrDefault(source, 0.0);
    }

    /**
     * Checks whether this interval immediately precedes {@code next} without any time gap.
     *
     * @param next candidate subsequent interval
     * @return {@code true} when {@code this.to().equals(next.from())}
     */
    public boolean isAdjacentBefore(GenerationInterval next) {
        return next != null && this.to.equals(next.from);
    }
}
