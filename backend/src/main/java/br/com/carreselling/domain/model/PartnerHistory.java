package br.com.carreselling.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PartnerHistory {

    private final UUID id;
    private final UUID partnerId;
    private final String name;
    private final String city;
    private final String phone;
    private final String email;
    private final BigDecimal commissionRate;
    private final Instant changedAt;
    private final String changedBy;

    public PartnerHistory(UUID id,
                          UUID partnerId,
                          String name,
                          String city,
                          String phone,
                          String email,
                          BigDecimal commissionRate,
                          Instant changedAt,
                          String changedBy) {
        this.id = id;
        this.partnerId = partnerId;
        this.name = name;
        this.city = city;
        this.phone = phone;
        this.email = email;
        this.commissionRate = commissionRate;
        this.changedAt = changedAt;
        this.changedBy = changedBy;
    }

    public UUID getId() { return id; }
    public UUID getPartnerId() { return partnerId; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public BigDecimal getCommissionRate() { return commissionRate; }
    public Instant getChangedAt() { return changedAt; }
    public String getChangedBy() { return changedBy; }
}
