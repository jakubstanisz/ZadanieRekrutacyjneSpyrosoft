package com.jakubstaniszewski.evcharging.backend.application.service;

import com.jakubstaniszewski.evcharging.backend.application.dto.BookingResult;
import com.jakubstaniszewski.evcharging.backend.application.dto.CreateBookingCommand;
import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingConflictException;
import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingValidationException;
import com.jakubstaniszewski.evcharging.backend.domain.exception.ForbiddenActionException;
import com.jakubstaniszewski.evcharging.backend.domain.exception.ResourceNotFoundException;
import com.jakubstaniszewski.evcharging.backend.domain.model.booking.Booking;
import com.jakubstaniszewski.evcharging.backend.domain.model.booking.Charger;
import com.jakubstaniszewski.evcharging.backend.domain.model.booking.TimeRange;
import com.jakubstaniszewski.evcharging.backend.domain.port.BookingRepository;
import com.jakubstaniszewski.evcharging.backend.domain.port.ChargerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link BookingApplicationService} using mocked persistence ports and a fixed UTC {@link Clock}.
 */
@ExtendWith(MockitoExtension.class)
class BookingApplicationServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-12-01T10:00:00Z");
    private static final UUID CHARGER_ID = UUID.fromString("11111111-1111-1111-1111-111111111101");
    private static final Charger CHARGER = new Charger(
            CHARGER_ID,
            "Hub-Alpha 150kW",
            "London Paddington, Bay 1",
            "CCS2",
            150
    );

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ChargerRepository chargerRepository;

    private BookingApplicationService service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        service = new BookingApplicationService(bookingRepository, chargerRepository, fixedClock);
    }

    @Test
    void shouldCreateBookingSuccessfullyWhenValidAndNoConflict() {
        Instant start = Instant.parse("2026-12-01T12:15:00Z");
        Instant end = Instant.parse("2026-12-01T15:15:00Z");
        CreateBookingCommand command = new CreateBookingCommand(CHARGER_ID, "alice", start, end);

        when(chargerRepository.findByIdWithLock(CHARGER_ID)).thenReturn(Optional.of(CHARGER));
        when(bookingRepository.findOverlappingByChargerId(CHARGER_ID, start, end)).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResult result = service.createBooking(command);

        assertThat(result.chargerId()).isEqualTo(CHARGER_ID);
        assertThat(result.chargerName()).isEqualTo("Hub-Alpha 150kW");
        assertThat(result.userId()).isEqualTo("alice");
        assertThat(result.startTime()).isEqualTo(start);
        assertThat(result.endTime()).isEqualTo(end);
        assertThat(result.durationHours()).isEqualTo(3);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void shouldThrowConflictExceptionWhenOverlappingBookingExists() {
        Instant start = Instant.parse("2026-12-01T12:00:00Z");
        Instant end = Instant.parse("2026-12-01T14:00:00Z");
        Booking existing = Booking.reconstitute(
                UUID.randomUUID(),
                CHARGER_ID,
                "bob",
                new TimeRange(Instant.parse("2026-12-01T13:00:00Z"), Instant.parse("2026-12-01T15:00:00Z")),
                FIXED_NOW
        );

        when(chargerRepository.findByIdWithLock(CHARGER_ID)).thenReturn(Optional.of(CHARGER));
        when(bookingRepository.findOverlappingByChargerId(CHARGER_ID, start, end)).thenReturn(List.of(existing));

        assertThatThrownBy(() -> service.createBooking(new CreateBookingCommand(CHARGER_ID, "alice", start, end)))
                .isInstanceOf(BookingConflictException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldRejectBookingStartingInThePast() {
        Instant pastStart = FIXED_NOW.minusSeconds(300);
        Instant end = pastStart.plusSeconds(7200);

        when(chargerRepository.findByIdWithLock(CHARGER_ID)).thenReturn(Optional.of(CHARGER));
        when(bookingRepository.findOverlappingByChargerId(CHARGER_ID, pastStart, end)).thenReturn(List.of());

        assertThatThrownBy(() -> service.createBooking(new CreateBookingCommand(CHARGER_ID, "alice", pastStart, end)))
                .isInstanceOf(BookingValidationException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenChargerDoesNotExist() {
        Instant start = FIXED_NOW.plusSeconds(3600);
        Instant end = start.plusSeconds(3600);

        when(chargerRepository.findByIdWithLock(CHARGER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createBooking(new CreateBookingCommand(CHARGER_ID, "alice", start, end)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldCancelOwnBookingAndRejectOtherUsers() {
        UUID bookingId = UUID.randomUUID();
        Booking existing = Booking.reconstitute(
                bookingId,
                CHARGER_ID,
                "alice",
                new TimeRange(FIXED_NOW.plusSeconds(3600), FIXED_NOW.plusSeconds(7200)),
                FIXED_NOW
        );
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.cancelBooking(bookingId, "bob"))
                .isInstanceOf(ForbiddenActionException.class);
        verify(bookingRepository, never()).deleteById(any());

        service.cancelBooking(bookingId, "alice");
        verify(bookingRepository).deleteById(bookingId);
    }
}
