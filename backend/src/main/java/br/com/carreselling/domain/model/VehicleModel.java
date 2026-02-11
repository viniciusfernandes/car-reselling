package br.com.carreselling.domain.model;

import java.time.Instant;
import java.util.UUID;

public class VehicleModel {

    private final UUID id;
    private final UUID brandId;
    private final String name;
    private final Instant createdAt;
    private final Instant updatedAt;

    public VehicleModel(UUID id, UUID brandId, String name, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.brandId = brandId;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBrandId() {
        return brandId;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
