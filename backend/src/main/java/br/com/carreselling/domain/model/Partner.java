package br.com.carreselling.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Partner {

    private final UUID id;
    private final String name;
    private final String city;
    private final BigDecimal commissionRate;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Partner(UUID id,
                   String name,
                   String city,
                   BigDecimal commissionRate,
                   Instant createdAt,
                   Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.commissionRate = commissionRate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
