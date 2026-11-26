package com.jakubstaniszewski.evcharging.backend.application.port.in;

import com.jakubstaniszewski.evcharging.backend.application.dto.BookingResult;
import com.jakubstaniszewski.evcharging.backend.application.dto.CreateBookingCommand;

/**
 * Input port for creating a new EV charger reservation.
 */
public interface CreateBookingUseCase {

    /**
     * Validates and persists a new charger booking.
     *
     * @param command booking creation parameters
     * @return created booking details
     */
    BookingResult createBooking(CreateBookingCommand command);
}
