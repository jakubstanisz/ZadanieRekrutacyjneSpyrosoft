package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence;

import com.jakubstaniszewski.evcharging.backend.domain.model.booking.Booking;
import com.jakubstaniszewski.evcharging.backend.domain.model.booking.Charger;
import com.jakubstaniszewski.evcharging.backend.domain.model.booking.TimeRange;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.adapter.BookingPersistenceAdapter;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.adapter.ChargerPersistenceAdapter;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JPA persistence tests using H2 in PostgreSQL mode verifying overlap SQL queries and locking lookups.
 */
@DataJpaTest
@Import({PersistenceMapper.class, ChargerPersistenceAdapter.class, BookingPersistenceAdapter.class})
class BookingPersistenceAdapterTest {

    private static final UUID CHARGER_1_ID = UUID.fromString("11111111-1111-1111-1111-111111111101");
    private static final UUID CHARGER_2_ID = UUID.fromString("11111111-1111-1111-1111-111111111102");

    @Autowired
    private ChargerPersistenceAdapter chargerAdapter;

    @Autowired
    private BookingPersistenceAdapter bookingAdapter;

    @BeforeEach
    void setUp() {
        chargerAdapter.saveAll(List.of(
                new Charger(CHARGER_1_ID, "Hub-Alpha", "London", "CCS2", 150),
                new Charger(CHARGER_2_ID, "Hub-Beta", "Manchester", "Type 2", 22)
        ));
    }

    @Test
    void shouldDetectOverlappingBookingsAndIgnoreAdjacentOrDifferentChargers() {
        Instant existingStart = Instant.parse("2026-12-01T12:00:00Z");
        Instant existingEnd = Instant.parse("2026-12-01T14:00:00Z");
        Booking saved = Booking.reconstitute(
                UUID.randomUUID(),
                CHARGER_1_ID,
                "alice",
                new TimeRange(existingStart, existingEnd),
                Instant.parse("2026-11-26T10:00:00Z")
        );
        bookingAdapter.save(saved);

        // Adjacent after [14:00, 16:00) -> NO overlap
        assertThat(bookingAdapter.findOverlappingByChargerId(
                CHARGER_1_ID,
                Instant.parse("2026-12-01T14:00:00Z"),
                Instant.parse("2026-12-01T16:00:00Z")
        )).isEmpty();

        // Adjacent before [10:00, 12:00) -> NO overlap
        assertThat(bookingAdapter.findOverlappingByChargerId(
                CHARGER_1_ID,
                Instant.parse("2026-12-01T10:00:00Z"),
                Instant.parse("2026-12-01T12:00:00Z")
        )).isEmpty();

        // Different charger during [12:00, 14:00) -> NO overlap
        assertThat(bookingAdapter.findOverlappingByChargerId(
                CHARGER_2_ID,
                existingStart,
                existingEnd
        )).isEmpty();

        // Overlapping [13:30, 15:30) on CHARGER_1 -> returns conflict
        List<Booking> overlaps = bookingAdapter.findOverlappingByChargerId(
                CHARGER_1_ID,
                Instant.parse("2026-12-01T13:30:00Z"),
                Instant.parse("2026-12-01T15:30:00Z")
        );
        assertThat(overlaps).hasSize(1);
        assertThat(overlaps.getFirst().getId()).isEqualTo(saved.getId());
    }

    @Test
    void shouldAcquireLockAndListUserBookingsOrderedByStartTime() {
        Booking later = Booking.reconstitute(
                UUID.randomUUID(),
                CHARGER_1_ID,
                "alice",
                new TimeRange(Instant.parse("2026-12-02T14:00:00Z"), Instant.parse("2026-12-02T16:00:00Z")),
                Instant.parse("2026-11-26T10:00:00Z")
        );
        Booking earlier = Booking.reconstitute(
                UUID.randomUUID(),
                CHARGER_2_ID,
                "alice",
                new TimeRange(Instant.parse("2026-12-01T09:00:00Z"), Instant.parse("2026-12-01T11:00:00Z")),
                Instant.parse("2026-11-26T10:05:00Z")
        );
        bookingAdapter.save(later);
        bookingAdapter.save(earlier);

        assertThat(chargerAdapter.findByIdWithLock(CHARGER_1_ID)).isPresent();
        List<Booking> userBookings = bookingAdapter.findByUserId("alice");
        assertThat(userBookings).extracting(Booking::getId)
                .containsExactly(earlier.getId(), later.getId());
    }
}
