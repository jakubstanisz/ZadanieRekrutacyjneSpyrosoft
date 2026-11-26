package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.mapper;

import com.jakubstaniszewski.evcharging.backend.domain.model.booking.Booking;
import com.jakubstaniszewski.evcharging.backend.domain.model.booking.Charger;
import com.jakubstaniszewski.evcharging.backend.domain.model.booking.TimeRange;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.entity.BookingJpaEntity;
import com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.entity.ChargerJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Manual bidirectional mapper between JPA persistence entities and pure domain models.
 */
@Component
public class PersistenceMapper {

    /**
     * Maps a {@link ChargerJpaEntity} to a domain {@link Charger}.
     *
     * @param entity JPA entity
     * @return domain model
     */
    public Charger toDomain(ChargerJpaEntity entity) {
        return new Charger(
                entity.getId(),
                entity.getName(),
                entity.getLocation(),
                entity.getConnectorType(),
                entity.getPowerKw()
        );
    }

    /**
     * Maps a domain {@link Charger} to a {@link ChargerJpaEntity}.
     *
     * @param charger domain model
     * @return JPA entity
     */
    public ChargerJpaEntity toEntity(Charger charger) {
        return new ChargerJpaEntity(
                charger.id(),
                charger.name(),
                charger.location(),
                charger.connectorType(),
                charger.powerKw()
        );
    }

    /**
     * Reconstitutes a domain {@link Booking} from a persisted {@link BookingJpaEntity}.
     *
     * @param entity JPA entity
     * @return domain aggregate
     */
    public Booking toDomain(BookingJpaEntity entity) {
        return Booking.reconstitute(
                entity.getId(),
                entity.getCharger().getId(),
                entity.getUserId(),
                new TimeRange(entity.getStartTime(), entity.getEndTime()),
                entity.getCreatedAt()
        );
    }

    /**
     * Maps a domain {@link Booking} to a {@link BookingJpaEntity}.
     *
     * @param booking    domain aggregate
     * @param chargerRef JPA charger reference
     * @return JPA entity
     */
    public BookingJpaEntity toEntity(Booking booking, ChargerJpaEntity chargerRef) {
        return new BookingJpaEntity(
                booking.getId(),
                chargerRef,
                booking.getUserId(),
                booking.getTimeRange().startTime(),
                booking.getTimeRange().endTime(),
                booking.getCreatedAt()
        );
    }
}
