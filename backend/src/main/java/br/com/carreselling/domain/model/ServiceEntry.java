package br.com.carreselling.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ServiceEntry {

    private final UUID id;
    private final UUID vehicleId;
    private ServiceType serviceType;
    private String description;
    private BigDecimal serviceValue;
    private LocalDate performedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public ServiceEntry(UUID id,
                        UUID vehicleId,
                        ServiceType serviceType,
                        String description,
                        BigDecimal serviceValue,
                        LocalDate performedAt,
                        Instant createdAt,
                        Instant updatedAt) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.serviceType = serviceType;
        this.description = description;
        this.serviceValue = serviceValue;
        this.performedAt = performedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getServiceValue() {
        return serviceValue;
    }

    public LocalDate getPerformedAt() {
        return performedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(ServiceType serviceType, String description, BigDecimal serviceValue, LocalDate performedAt) {
        this.serviceType = serviceType;
        this.description = description;
        this.serviceValue = serviceValue;
        this.performedAt = performedAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
