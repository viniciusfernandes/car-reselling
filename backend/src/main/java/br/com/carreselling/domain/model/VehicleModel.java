package br.com.carreselling.domain.model;

import java.time.Instant;
import java.util.UUID;

public class VehicleModel {

    private final UUID id;
    private final int companyId;
    private final UUID brandId;
    private final String name;
    private final Instant createdAt;
    private final Instant updatedAt;

    public VehicleModel(UUID id, int companyId, UUID brandId, String name, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.companyId = companyId;
        this.brandId = brandId;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public int getCompanyId() {
        return companyId;
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
