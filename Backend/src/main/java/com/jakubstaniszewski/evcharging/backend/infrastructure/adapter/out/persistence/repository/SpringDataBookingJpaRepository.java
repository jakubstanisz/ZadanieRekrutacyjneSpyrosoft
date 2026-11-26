package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.repository;

import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.entity.BookingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link BookingJpaEntity}.
 */
public interface SpringDataBookingJpaRepository extends JpaRepository<BookingJpaEntity, UUID> {

    List<BookingJpaEntity> findByUserIdOrderByStartTimeAsc(String userId);

    List<BookingJpaEntity> findByChargerIdOrderByStartTimeAsc(UUID chargerId);

    /**
     * Finds bookings for the given charger overlapping {@code [startTime, endTime)}.
     *
     * @param chargerId target charger identifier
     * @param startTime candidate start time
     * @param endTime   candidate end time
     * @return overlapping booking entities
     */
    @Query("""
            SELECT b FROM BookingJpaEntity b
            WHERE b.charger.id = :chargerId
              AND b.startTime < :endTime
              AND b.endTime > :startTime
            ORDER BY b.startTime ASC
            """)
    List<BookingJpaEntity> findOverlapping(
            @Param("chargerId") UUID chargerId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime
    );
}
