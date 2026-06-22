package br.com.carreselling.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Brand {

    private final UUID id;
    private final int companyId;
    private final String name;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Brand(UUID id, int companyId, String name, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.companyId = companyId;
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
