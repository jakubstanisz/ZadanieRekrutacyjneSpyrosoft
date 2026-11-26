package com.jakubstaniszewski.evcharging.backend.application.port.in;

import java.util.UUID;

/**
 * Input port for cancelling an existing charger reservation.
 */
public interface CancelBookingUseCase {

    /**
     * Cancels a booking if it exists and belongs to the requesting user.
     *
     * @param bookingId booking identifier
     * @param userId    requesting user identifier
     */
    void cancelBooking(UUID bookingId, String userId);
}
