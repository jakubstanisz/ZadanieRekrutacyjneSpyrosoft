package com.jakubstaniszewski.evcharging.backend.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * JPA entity mapping the {@code chargers} database table.
 */
@Entity
@Table(name = "chargers")
public class ChargerJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "location", nullable = false, length = 255)
    private String location;

    @Column(name = "connector_type", nullable = false, length = 50)
    private String connectorType;

    @Column(name = "power_kw", nullable = false)
    private int powerKw;

    protected ChargerJpaEntity() {
    }

    /**
     * Creates a new JPA charger entity.
     *
     * @param id            unique charger identifier
     * @param name          charger name or code
     * @param location      physical location
     * @param connectorType connector standard
     * @param powerKw       maximum charging power in kilowatts
     */
    public ChargerJpaEntity(UUID id, String name, String location, String connectorType, int powerKw) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.connectorType = connectorType;
        this.powerKw = powerKw;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public int getPowerKw() {
        return powerKw;
    }
}
