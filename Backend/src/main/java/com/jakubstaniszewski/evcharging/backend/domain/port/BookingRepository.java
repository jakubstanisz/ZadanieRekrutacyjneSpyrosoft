package com.jakubstaniszewski.evcharging.backend.domain.port;

import com.jakubstaniszewski.evcharging.backend.domain.model.booking.Booking;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for persisting and querying {@link Booking} aggregates.
 */
public interface BookingRepository {

    /**
     * Persists a booking.
     *
     * @param booking booking aggregate to store
     * @return persisted booking
     */
    Booking save(Booking booking);

    /**
     * Finds a booking by its unique identifier.
     *
     * @param id booking identifier
     * @return matching booking if present
     */
    Optional<Booking> findById(UUID id);

    /**
     * Returns all bookings created by the specified user.
     *
     * @param userId user identifier
     * @return list of user's bookings ordered by start time
     */
    List<Booking> findByUserId(String userId);

    /**
     * Returns all bookings for a given charger ordered by start time.
     *
     * @param chargerId charger identifier
     * @return list of bookings on the charger
     */
    List<Booking> findByChargerId(UUID chargerId);

    /**
     * Finds existing bookings on a charger that overlap with {@code [startTime, endTime)}.
     *
     * @param chargerId charger identifier
     * @param startTime candidate start time
     * @param endTime   candidate end time
     * @return overlapping bookings
     */
    List<Booking> findOverlappingByChargerId(UUID chargerId, Instant startTime, Instant endTime);

    /**
     * Deletes a booking by its identifier.
     *
     * @param id booking identifier
     */
    void deleteById(UUID id);
}
