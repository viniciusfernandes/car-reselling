package br.com.carreselling.usecase.payment.list.contract;

import br.com.carreselling.domain.model.PaymentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentItem(
    UUID id,
    PaymentType paymentType,
    String description,
    BigDecimal amount,
    LocalDate paymentDate,
    UUID vehicleId,
    String vehicleLicensePlate,
    String notes,
    Instant createdAt,
    Instant updatedAt
) {}
