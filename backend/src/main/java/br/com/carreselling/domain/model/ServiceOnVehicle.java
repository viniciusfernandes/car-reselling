package br.com.carreselling.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ServiceOnVehicle {

    private final UUID id;
    private final UUID vehicleId;
    private ServiceType serviceType;
    private String description;
    private BigDecimal serviceValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant createdAt;
    private Instant updatedAt;

    public ServiceOnVehicle(UUID id,
                            UUID vehicleId,
                            LocalDate startDate,
                            LocalDate endDate) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public ServiceOnVehicle(UUID id,
                            UUID vehicleId,
                            ServiceType serviceType,
                            String description,
                            BigDecimal serviceValue,
                            LocalDate startDate,
                            LocalDate endDate,
                            Instant createdAt,
                            Instant updatedAt) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.serviceType = serviceType;
        this.description = description;
        this.serviceValue = serviceValue;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(ServiceType serviceType, String description, BigDecimal serviceValue,
                       LocalDate startDate, LocalDate endDate) {
        this.serviceType = serviceType;
        this.description = description;
        this.serviceValue = serviceValue;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
