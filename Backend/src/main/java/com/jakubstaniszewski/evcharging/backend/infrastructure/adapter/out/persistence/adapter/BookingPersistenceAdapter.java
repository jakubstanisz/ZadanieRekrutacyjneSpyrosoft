package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.adapter;

import com.jakubstaniszewski.evcharging.backend.domain.exception.BookingConflictException;
import com.jakubstaniszewski.evcharging.backend.domain.model.booking.Booking;
import com.jakubstaniszewski.evcharging.backend.domain.port.BookingRepository;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.entity.BookingJpaEntity;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.entity.ChargerJpaEntity;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.repository.SpringDataBookingJpaRepository;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.repository.SpringDataChargerJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA persistence adapter implementing the {@link BookingRepository} domain port.
 */
@Repository
public class BookingPersistenceAdapter implements BookingRepository {

    private final SpringDataBookingJpaRepository bookingJpaRepository;
    private final SpringDataChargerJpaRepository chargerJpaRepository;
    private final PersistenceMapper mapper;

    /**
     * Creates a new {@link BookingPersistenceAdapter}.
     *
     * @param bookingJpaRepository Spring Data booking repository
     * @param chargerJpaRepository Spring Data charger repository
     * @param mapper               entity-domain mapper
     */
    public BookingPersistenceAdapter(
            SpringDataBookingJpaRepository bookingJpaRepository,
            SpringDataChargerJpaRepository chargerJpaRepository,
            PersistenceMapper mapper
    ) {
        this.bookingJpaRepository = bookingJpaRepository;
        this.chargerJpaRepository = chargerJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Booking save(Booking booking) {
        try {
            ChargerJpaEntity chargerRef = chargerJpaRepository.getReferenceById(booking.getChargerId());
            BookingJpaEntity saved = bookingJpaRepository.save(mapper.toEntity(booking, chargerRef));
            return mapper.toDomain(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new BookingConflictException(
                    "Booking violates database integrity constraints for charger " + booking.getChargerId()
            );
        }
    }

    @Override
    public Optional<Booking> findById(UUID id) {
        return bookingJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Booking> findByUserId(String userId) {
        return bookingJpaRepository.findByUserIdOrderByStartTimeAsc(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Booking> findByChargerId(UUID chargerId) {
        return bookingJpaRepository.findByChargerIdOrderByStartTimeAsc(chargerId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Booking> findOverlappingByChargerId(UUID chargerId, Instant startTime, Instant endTime) {
        return bookingJpaRepository.findOverlapping(chargerId, startTime, endTime).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        bookingJpaRepository.deleteById(id);
    }
}
