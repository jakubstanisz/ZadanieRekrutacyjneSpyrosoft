package com.jakubstaniszewski.evcharging.backend.application.service;

import com.jakubstaniszewski.evcharging.backend.application.dto.BookingResult;
import com.jakubstaniszewski.evcharging.backend.application.dto.CreateBookingCommand;
import com.jakubstaniszewski.evcharging.backend.application.port.in.CancelBookingUseCase;
import com.jakubstaniszewski.evcharging.backend.application.port.in.CreateBookingUseCase;
import com.jakubstaniszewski.evcharging.backend.application.port.in.GetUserBookingsUseCase;
import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingValidationException;
import com.jakubstaniszewski.evcharging.backend.domain.exception.ResourceNotFoundException;
import com.jakubstaniszewski.evcharging.backend.domain.model.booking.Booking;
import com.jakubstaniszewski.evcharging.backend.domain.model.booking.Charger;
import com.jakubstaniszewski.evcharging.backend.domain.model.booking.TimeRange;
import com.jakubstaniszewski.evcharging.backend.domain.port.BookingRepository;
import com.jakubstaniszewski.evcharging.backend.domain.port.ChargerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Application service orchestrating creation, retrieval, and cancellation of EV charger bookings.
 */
@Service
public class BookingApplicationService implements
        CreateBookingUseCase,
        GetUserBookingsUseCase,
        CancelBookingUseCase {

    private final BookingRepository bookingRepository;
    private final ChargerRepository chargerRepository;
    private final Clock clock;

    /**
     * Creates a new {@link BookingApplicationService}.
     *
     * @param bookingRepository booking persistence port
     * @param chargerRepository charger persistence port
     * @param clock             UTC clock reference
     */
    public BookingApplicationService(
            BookingRepository bookingRepository,
            ChargerRepository chargerRepository,
            Clock clock
    ) {
        this.bookingRepository = bookingRepository;
        this.chargerRepository = chargerRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public BookingResult createBooking(CreateBookingCommand command) {
        if (command == null) {
            throw new BookingValidationException("Booking command must not be null");
        }
        if (command.chargerId() == null) {
            throw new BookingValidationException("Charger id must not be null");
        }
        String normalizedUserId = validateUserId(command.userId());
        TimeRange requestedRange = new TimeRange(command.startTime(), command.endTime());

        // Acquire pessimistic write lock on the charger row to serialize concurrent bookings for the same charger.
        Charger charger = chargerRepository.findByIdWithLock(command.chargerId())
                .orElseThrow(() -> new ResourceNotFoundException("Charger not found: " + command.chargerId()));

        List<Booking> overlappingBookings = bookingRepository.findOverlappingByChargerId(
                charger.id(),
                requestedRange.startTime(),
                requestedRange.endTime()
        );

        Booking booking = Booking.create(
                UUID.randomUUID(),
                charger.id(),
                normalizedUserId,
                requestedRange,
                clock.instant(),
                overlappingBookings
        );

        Booking savedBooking = bookingRepository.save(booking);
        return toBookingResult(savedBooking, charger);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResult> getUserBookings(String userId) {
        String normalizedUserId = validateUserId(userId);
        List<Booking> bookings = bookingRepository.findByUserId(normalizedUserId);
        if (bookings.isEmpty()) {
            return List.of();
        }

        Map<UUID, Charger> chargersById = chargerRepository.findAll().stream()
                .collect(Collectors.toMap(Charger::id, Function.identity()));

        return bookings.stream()
                .map(booking -> toBookingResult(booking, chargersById.get(booking.getChargerId())))
                .toList();
    }

    @Override
    @Transactional
    public void cancelBooking(UUID bookingId, String userId) {
        if (bookingId == null) {
            throw new BookingValidationException("Booking id must not be null");
        }
        String normalizedUserId = validateUserId(userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        booking.assertCanBeCancelledBy(normalizedUserId);
        bookingRepository.deleteById(booking.getId());
    }

    private String validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BookingValidationException("User id must not be blank");
        }
        return userId.trim();
    }

    private BookingResult toBookingResult(Booking booking, Charger charger) {
        String chargerName = charger != null ? charger.name() : "Unknown charger";
        String chargerLocation = charger != null ? charger.location() : "";
        return new BookingResult(
                booking.getId(),
                booking.getChargerId(),
                chargerName,
                chargerLocation,
                booking.getUserId(),
                booking.getTimeRange().startTime(),
                booking.getTimeRange().endTime(),
                booking.getTimeRange().durationInHours(),
                booking.getCreatedAt()
        );
    }
}
