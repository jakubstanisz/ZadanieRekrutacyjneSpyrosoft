package com.jakubstaniszewski.evcharging.backend.domain.model.booking;

import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingConflictException;
import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingValidationException;
import com.jakubstaniszewski.evcharging.backend.domain.exception.ForbiddenActionException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link Booking} factory invariants and ownership cancellation checks.
 */
class BookingTest {

    private static final UUID CHARGER_ID = UUID.fromString("11111111-1111-1111-1111-111111111101");
    private static final Instant NOW = Instant.parse("2026-12-01T10:00:00Z");

    @Test
    void shouldCreateBookingWhenAllSpecificationsPass() {
        TimeRange slot = new TimeRange(
                Instant.parse("2026-12-01T12:15:00Z"),
                Instant.parse("2026-12-01T14:15:00Z")
        );
        Booking adjacentExisting = Booking.reconstitute(
                UUID.randomUUID(),
                CHARGER_ID,
                "other-user",
                new TimeRange(Instant.parse("2026-12-01T10:15:00Z"), Instant.parse("2026-12-01T12:15:00Z")),
                NOW.minusSeconds(3600)
        );

        Booking booking = Booking.create(
                UUID.randomUUID(),
                CHARGER_ID,
                "user-1",
                slot,
                NOW,
                List.of(adjacentExisting)
        );

        assertThat(booking.getChargerId()).isEqualTo(CHARGER_ID);
        assertThat(booking.getUserId()).isEqualTo("user-1");
        assertThat(booking.getTimeRange()).isEqualTo(slot);
    }

    @Test
    void shouldRejectBookingStartingInThePast() {
        TimeRange pastSlot = new TimeRange(NOW.minusSeconds(60), NOW.plusSeconds(3540));

        assertThatThrownBy(() -> Booking.create(UUID.randomUUID(), CHARGER_ID, "user-1", pastSlot, NOW, List.of()))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("past");
    }

    @Test
    void shouldRejectBookingWithDurationOutsideOneToSixWholeHours() {
        TimeRange sevenHours = new TimeRange(NOW.plusSeconds(3600), NOW.plusSeconds(8 * 3600));
        TimeRange ninetyMinutes = new TimeRange(NOW.plusSeconds(3600), NOW.plusSeconds(3600 + 5400));

        assertThatThrownBy(() -> Booking.create(UUID.randomUUID(), CHARGER_ID, "user-1", sevenHours, NOW, List.of()))
                .isInstanceOf(BookingValidationException.class);
        assertThatThrownBy(() -> Booking.create(UUID.randomUUID(), CHARGER_ID, "user-1", ninetyMinutes, NOW, List.of()))
                .isInstanceOf(BookingValidationException.class);
    }

    @Test
    void shouldRejectOverlappingBookingOnSameCharger() {
        Booking existing = Booking.reconstitute(
                UUID.randomUUID(),
                CHARGER_ID,
                "other-user",
                new TimeRange(Instant.parse("2026-12-01T12:00:00Z"), Instant.parse("2026-12-01T15:00:00Z")),
                NOW
        );
        TimeRange overlapping = new TimeRange(
                Instant.parse("2026-12-01T14:00:00Z"),
                Instant.parse("2026-12-01T16:00:00Z")
        );

        assertThatThrownBy(() -> Booking.create(
                UUID.randomUUID(),
                CHARGER_ID,
                "user-1",
                overlapping,
                NOW,
                List.of(existing)
        )).isInstanceOf(BookingConflictException.class);
    }

    @Test
    void shouldEnforceOwnershipOnCancellation() {
        Booking booking = Booking.reconstitute(
                UUID.randomUUID(),
                CHARGER_ID,
                "owner-user",
                new TimeRange(NOW.plusSeconds(3600), NOW.plusSeconds(7200)),
                NOW
        );

        assertThatNoException().isThrownBy(() -> booking.assertCanBeCancelledBy("owner-user"));
        assertThatThrownBy(() -> booking.assertCanBeCancelledBy("intruder"))
                .isInstanceOf(ForbiddenActionException.class);
    }
}
