package com.jakubstaniszewski.evcharging.backend.domain.model.booking;

import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link TimeRange} boundary overlap and whole-hour duration rules.
 */
class TimeRangeTest {

    @Test
    void shouldRejectEndBeforeOrEqualToStart() {
        Instant start = Instant.parse("2026-12-01T12:00:00Z");

        assertThatThrownBy(() -> new TimeRange(start, start))
                .isInstanceOf(BookingValidationException.class);
        assertThatThrownBy(() -> new TimeRange(start, start.minusSeconds(60)))
                .isInstanceOf(BookingValidationException.class);
    }

    @Test
    void shouldNotOverlapWhenAdjacentAtBoundary() {
        TimeRange first = new TimeRange(
                Instant.parse("2026-12-01T12:00:00Z"),
                Instant.parse("2026-12-01T14:00:00Z")
        );
        TimeRange immediatelyAfter = new TimeRange(
                Instant.parse("2026-12-01T14:00:00Z"),
                Instant.parse("2026-12-01T16:00:00Z")
        );
        TimeRange immediatelyBefore = new TimeRange(
                Instant.parse("2026-12-01T10:00:00Z"),
                Instant.parse("2026-12-01T12:00:00Z")
        );

        assertThat(first.overlaps(immediatelyAfter)).isFalse();
        assertThat(immediatelyAfter.overlaps(first)).isFalse();
        assertThat(first.overlaps(immediatelyBefore)).isFalse();
    }

    @Test
    void shouldDetectPartialOverlapAndContainment() {
        TimeRange base = new TimeRange(
                Instant.parse("2026-12-01T12:00:00Z"),
                Instant.parse("2026-12-01T15:00:00Z")
        );
        TimeRange partialRight = new TimeRange(
                Instant.parse("2026-12-01T14:30:00Z"),
                Instant.parse("2026-12-01T16:30:00Z")
        );
        TimeRange inner = new TimeRange(
                Instant.parse("2026-12-01T13:00:00Z"),
                Instant.parse("2026-12-01T14:00:00Z")
        );
        TimeRange outer = new TimeRange(
                Instant.parse("2026-12-01T11:00:00Z"),
                Instant.parse("2026-12-01T17:00:00Z")
        );

        assertThat(base.overlaps(partialRight)).isTrue();
        assertThat(partialRight.overlaps(base)).isTrue();
        assertThat(base.overlaps(inner)).isTrue();
        assertThat(inner.overlaps(base)).isTrue();
        assertThat(base.overlaps(outer)).isTrue();
        assertThat(base.overlaps(base)).isTrue();
    }

    @Test
    void shouldValidateWholeHoursWithArbitraryStartMinute() {
        TimeRange twoHoursFromQuarterPast = new TimeRange(
                Instant.parse("2026-12-01T14:15:00Z"),
                Instant.parse("2026-12-01T16:15:00Z")
        );
        TimeRange twoAndHalfHours = new TimeRange(
                Instant.parse("2026-12-01T14:15:00Z"),
                Instant.parse("2026-12-01T16:45:00Z")
        );
        TimeRange sevenHours = new TimeRange(
                Instant.parse("2026-12-01T10:00:00Z"),
                Instant.parse("2026-12-01T17:00:00Z")
        );

        assertThat(twoHoursFromQuarterPast.isWholeHoursBetween(1, 6)).isTrue();
        assertThat(twoAndHalfHours.isWholeHoursBetween(1, 6)).isFalse();
        assertThat(sevenHours.isWholeHoursBetween(1, 6)).isFalse();
    }
}
