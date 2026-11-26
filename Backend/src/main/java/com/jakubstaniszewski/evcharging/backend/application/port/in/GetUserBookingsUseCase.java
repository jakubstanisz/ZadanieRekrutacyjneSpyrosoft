package com.jakubstaniszewski.evcharging.backend.application.port.in;

import com.jakubstaniszewski.evcharging.backend.application.dto.BookingResult;

import java.util.List;

/**
 * Input port for retrieving bookings owned by a specific user.
 */
public interface GetUserBookingsUseCase {

    /**
     * Lists all bookings belonging to the given user.
     *
     * @param userId user identifier
     * @return list of user's bookings ordered by start time
     */
    List<BookingResult> getUserBookings(String userId);
}
