package br.com.carreselling.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Payment {

    public final UUID id;
    public PaymentType paymentType;
    public String description;
    public BigDecimal amount;
    public LocalDate paymentDate;
    public UUID vehicleId;
    public String vehicleLicensePlate;
    public String notes;
    public Instant createdAt;
    public Instant updatedAt;

    public Payment(UUID id,
                   PaymentType paymentType,
                   String description,
                   BigDecimal amount,
                   LocalDate paymentDate,
                   UUID vehicleId,
                   String notes,
                   Instant createdAt,
                   Instant updatedAt) {
        this.id = id;
        this.paymentType = paymentType;
        this.description = description;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.vehicleId = vehicleId;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void update(PaymentType paymentType,
                       String description,
                       BigDecimal amount,
                       LocalDate paymentDate,
                       UUID vehicleId,
                       String notes) {
        this.paymentType = paymentType;
        this.description = description;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.vehicleId = vehicleId;
        this.notes = notes;
    }
}
