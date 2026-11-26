package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.repository;

import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.entity.ChargerJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ChargerJpaEntity}.
 */
public interface SpringDataChargerJpaRepository extends JpaRepository<ChargerJpaEntity, UUID> {

    List<ChargerJpaEntity> findAllByOrderByNameAsc();

    /**
     * Loads a charger entity with a {@code PESSIMISTIC_WRITE} database lock to serialize concurrent bookings.
     *
     * @param id charger identifier
     * @return locked charger entity if present
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ChargerJpaEntity c WHERE c.id = :id")
    Optional<ChargerJpaEntity> findByIdWithLock(@Param("id") UUID id);
}
