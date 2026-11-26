package com.jakubstaniszewski.evcharging.backend.domain.model.booking;

import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingValidationException;
import com.jakubstaniszewski.evcharging.backend.domain.exception.ForbiddenActionException;
import com.jakubstaniszewski.evcharging.backend.domain.specification.BookingCandidate;
import com.jakubstaniszewski.evcharging.backend.domain.specification.BookingDurationSpecification;
import com.jakubstaniszewski.evcharging.backend.domain.specification.BookingSpecification;
import com.jakubstaniszewski.evcharging.backend.domain.specification.FutureStartTimeSpecification;
import com.jakubstaniszewski.evcharging.backend.domain.specification.NoBookingOverlapSpecification;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain aggregate representing a user's reservation of a specific EV charger for a given time range.
 */
public final class Booking {

    private static final List<BookingSpecification> DEFAULT_SPECIFICATIONS = List.of(
            new FutureStartTimeSpecification(),
            new BookingDurationSpecification(),
            new NoBookingOverlapSpecification()
    );

    private final UUID id;
    private final UUID chargerId;
    private final String userId;
    private final TimeRange timeRange;
    private final Instant createdAt;

    private Booking(UUID id, UUID chargerId, String userId, TimeRange timeRange, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "Booking id must not be null");
        this.chargerId = Objects.requireNonNull(chargerId, "Charger id must not be null");
        if (userId == null || userId.isBlank()) {
            throw new BookingValidationException("User id must not be blank");
        }
        this.userId = userId.trim();
        this.timeRange = Objects.requireNonNull(timeRange, "Time range must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created timestamp must not be null");
    }

    /**
     * Factory method creating a new booking after validating all standard domain specifications.
     *
     * @param id                      generated unique booking identifier
     * @param chargerId               target charger identifier
     * @param userId                  identifier of the booking owner
     * @param timeRange               requested booking time interval
     * @param currentTime             current UTC reference timestamp
     * @param existingChargerBookings existing bookings for the target charger
     * @return validated {@link Booking} instance
     */
    public static Booking create(
            UUID id,
            UUID chargerId,
            String userId,
            TimeRange timeRange,
            Instant currentTime,
            List<Booking> existingChargerBookings
    ) {
        return create(id, chargerId, userId, timeRange, currentTime, existingChargerBookings, DEFAULT_SPECIFICATIONS);
    }

    /**
     * Factory method creating a new booking evaluated against a provided set of specifications.
     *
     * @param id                      generated unique booking identifier
     * @param chargerId               target charger identifier
     * @param userId                  identifier of the booking owner
     * @param timeRange               requested booking time interval
     * @param currentTime             current UTC reference timestamp
     * @param existingChargerBookings existing bookings for the target charger
     * @param specifications          domain specifications to enforce
     * @return validated {@link Booking} instance
     */
    public static Booking create(
            UUID id,
            UUID chargerId,
            String userId,
            TimeRange timeRange,
            Instant currentTime,
            List<Booking> existingChargerBookings,
            List<BookingSpecification> specifications
    ) {
        Objects.requireNonNull(currentTime, "Current timestamp must not be null");
        Objects.requireNonNull(specifications, "Specifications list must not be null");
        List<Booking> safeExisting = existingChargerBookings == null ? List.of() : List.copyOf(existingChargerBookings);

        BookingCandidate candidate = new BookingCandidate(chargerId, userId, timeRange, currentTime, safeExisting);
        specifications.forEach(spec -> spec.validate(candidate));

        return new Booking(id, chargerId, userId, timeRange, currentTime);
    }

    /**
     * Reconstitutes an already persisted booking without re-running creation-time temporal checks.
     *
     * @param id        persisted booking identifier
     * @param chargerId charger identifier
     * @param userId    owner identifier
     * @param timeRange booked time interval
     * @param createdAt creation timestamp
     * @return reconstituted {@link Booking}
     */
    public static Booking reconstitute(
            UUID id,
            UUID chargerId,
            String userId,
            TimeRange timeRange,
            Instant createdAt
    ) {
        return new Booking(id, chargerId, userId, timeRange, createdAt);
    }

    /**
     * Checks if this booking's time range overlaps with another time range.
     *
     * @param other time range to test
     * @return {@code true} if the intervals overlap
     */
    public boolean overlaps(TimeRange other) {
        return this.timeRange.overlaps(other);
    }

    /**
     * Checks whether this booking belongs to the specified user.
     *
     * @param candidateUserId user identifier to check
     * @return {@code true} if owned by {@code candidateUserId}
     */
    public boolean isOwnedBy(String candidateUserId) {
        return candidateUserId != null && this.userId.equals(candidateUserId.trim());
    }

    /**
     * Ensures the specified user is allowed to cancel this booking.
     *
     * @param requestingUserId user requesting cancellation
     * @throws ForbiddenActionException if the booking belongs to a different user
     */
    public void assertCanBeCancelledBy(String requestingUserId) {
        if (!isOwnedBy(requestingUserId)) {
            throw new ForbiddenActionException("You are not allowed to cancel another user's booking");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getChargerId() {
        return chargerId;
    }

    public String getUserId() {
        return userId;
    }

    public TimeRange getTimeRange() {
        return timeRange;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Booking other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
