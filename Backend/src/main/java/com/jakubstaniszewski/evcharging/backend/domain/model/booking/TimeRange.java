package com.jakubstaniszewski.evcharging.backend.domain.model.booking;

import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingValidationException;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Value Object representing a half-open UTC booking time interval {@code [startTime, endTime)}.
 *
 * @param startTime inclusive booking start timestamp in UTC
 * @param endTime   exclusive booking end timestamp in UTC
 */
public record TimeRange(Instant startTime, Instant endTime) {

    private static final long SECONDS_PER_HOUR = 3600L;

    /**
     * Validates that both timestamps are present and {@code endTime} is strictly after {@code startTime}.
     */
    public TimeRange {
        if (startTime == null || endTime == null) {
            throw new BookingValidationException("Start time and end time must not be null");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BookingValidationException("End time must be strictly after start time");
        }
    }

    /**
     * Determines whether this half-open interval overlaps with another interval.
     * Adjacent intervals (where one ends exactly when the other starts) do not overlap.
     *
     * @param other another time range to compare against
     * @return {@code true} if {@code startA < endB && endA > startB}
     */
    public boolean overlaps(TimeRange other) {
        Objects.requireNonNull(other, "Comparison time range must not be null");
        return this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime);
    }

    /**
     * Checks whether the duration is an exact whole number of hours within {@code [minHours, maxHours]}.
     *
     * @param minHours minimum allowed whole hours
     * @param maxHours maximum allowed whole hours
     * @return {@code true} if duration has zero leftover minutes/seconds/nanos and falls in range
     */
    public boolean isWholeHoursBetween(int minHours, int maxHours) {
        Duration duration = Duration.between(startTime, endTime);
        if (duration.getNano() != 0 || duration.getSeconds() % SECONDS_PER_HOUR != 0) {
            return false;
        }
        long hours = duration.toHours();
        return hours >= minHours && hours <= maxHours;
    }

    /**
     * Returns the duration of this time range in whole hours.
     *
     * @return duration in hours
     */
    public long durationInHours() {
        return Duration.between(startTime, endTime).toHours();
    }
}
