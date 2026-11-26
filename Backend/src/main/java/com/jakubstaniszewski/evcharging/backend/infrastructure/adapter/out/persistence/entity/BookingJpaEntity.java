package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping the {@code bookings} database table with a composite index
 * on {@code (charger_id, start_time, end_time)} for fast overlap queries.
 */
@Entity
@Table(
        name = "bookings",
        indexes = @Index(
                name = "idx_bookings_charger_time",
                columnList = "charger_id, start_time, end_time"
        )
)
public class BookingJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "charger_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_bookings_charger")
    )
    private ChargerJpaEntity charger;

    @Column(name = "user_id", nullable = false, length = 120)
    private String userId;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected BookingJpaEntity() {
    }

    /**
     * Creates a new JPA booking entity.
     *
     * @param id        unique booking identifier
     * @param charger   referenced charger entity
     * @param userId    owner user identifier
     * @param startTime inclusive UTC start timestamp
     * @param endTime   exclusive UTC end timestamp
     * @param createdAt UTC creation timestamp
     */
    public BookingJpaEntity(
            UUID id,
            ChargerJpaEntity charger,
            String userId,
            Instant startTime,
            Instant endTime,
            Instant createdAt
    ) {
        this.id = id;
        this.charger = charger;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public ChargerJpaEntity getCharger() {
        return charger;
    }

    public String getUserId() {
        return userId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
