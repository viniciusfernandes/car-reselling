package br.com.carreselling.application.service.model;

import br.com.carreselling.domain.model.PaymentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentSummary(
    UUID id,
    PaymentType paymentType,
    String description,
    BigDecimal amount,
    LocalDate paymentDate,
    UUID vehicleId,
    String vehicleLicensePlate,
    Integer referenceYear,
    Integer referenceMonth,
    String notes,
    Instant createdAt,
    Instant updatedAt
) {}
