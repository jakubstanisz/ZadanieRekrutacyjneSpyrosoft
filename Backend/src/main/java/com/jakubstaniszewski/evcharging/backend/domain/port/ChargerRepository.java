package com.jakubstaniszewski.evcharging.backend.domain.port;

import com.jakubstaniszewski.evcharging.backend.domain.model.booking.Charger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for accessing and seeding {@link Charger} aggregates.
 */
public interface ChargerRepository {

    /**
     * Returns all registered chargers ordered consistently.
     *
     * @return list of chargers
     */
    List<Charger> findAll();

    /**
     * Finds a charger by its unique identifier.
     *
     * @param id charger identifier
     * @return matching charger if present
     */
    Optional<Charger> findById(UUID id);

    /**
     * Finds a charger by id and acquires a pessimistic write lock for concurrent booking safety.
     *
     * @param id charger identifier
     * @return matching charger if present
     */
    Optional<Charger> findByIdWithLock(UUID id);

    /**
     * Persists a batch of chargers (used primarily for initial data seeding).
     *
     * @param chargers chargers to save
     * @return persisted chargers
     */
    List<Charger> saveAll(List<Charger> chargers);

    /**
     * Returns total count of persisted chargers.
     *
     * @return number of chargers
     */
    long count();
}
