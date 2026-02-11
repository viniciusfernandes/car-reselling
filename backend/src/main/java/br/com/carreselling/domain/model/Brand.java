package br.com.carreselling.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Brand {

    private final UUID id;
    private final String name;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Brand(UUID id, String name, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
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
