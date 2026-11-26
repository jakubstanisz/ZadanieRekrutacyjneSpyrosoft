package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.adapter;

import com.jakubstaniszewski.evcharging.backend.domain.model.booking.Charger;
import com.jakubstaniszewski.evcharging.backend.domain.port.ChargerRepository;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.entity.ChargerJpaEntity;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.repository.SpringDataChargerJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA persistence adapter implementing the {@link ChargerRepository} domain port.
 */
@Repository
public class ChargerPersistenceAdapter implements ChargerRepository {

    private final SpringDataChargerJpaRepository jpaRepository;
    private final PersistenceMapper mapper;

    /**
     * Creates a new {@link ChargerPersistenceAdapter}.
     *
     * @param jpaRepository Spring Data charger repository
     * @param mapper        entity-domain mapper
     */
    public ChargerPersistenceAdapter(
            SpringDataChargerJpaRepository jpaRepository,
            PersistenceMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Charger> findAll() {
        return jpaRepository.findAllByOrderByNameAsc().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Charger> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Charger> findByIdWithLock(UUID id) {
        return jpaRepository.findByIdWithLock(id).map(mapper::toDomain);
    }

    @Override
    public List<Charger> saveAll(List<Charger> chargers) {
        List<ChargerJpaEntity> entities = chargers.stream()
                .map(mapper::toEntity)
                .toList();
        return jpaRepository.saveAll(entities).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
