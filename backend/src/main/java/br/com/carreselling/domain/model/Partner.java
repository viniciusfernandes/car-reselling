package br.com.carreselling.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Partner {

    private final UUID id;
    private final String name;
    private final String city;
    private final String phone;
    private final String email;
    private final BigDecimal commissionRate;
    private final boolean enabled;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Partner(UUID id,
                   String name,
                   String city,
                   String phone,
                   String email,
                   BigDecimal commissionRate,
                   boolean enabled,
                   Instant createdAt,
                   Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.phone = phone;
        this.email = email;
        this.commissionRate = commissionRate;
        this.enabled = enabled;
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

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
