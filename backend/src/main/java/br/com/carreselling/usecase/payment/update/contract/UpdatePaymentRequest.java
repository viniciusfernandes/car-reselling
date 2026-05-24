package br.com.carreselling.usecase.payment.update.contract;

import br.com.carreselling.domain.model.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdatePaymentRequest(
    @NotNull PaymentType paymentType,
    String description,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotNull LocalDate paymentDate,
    String vehicleLicensePlate,
    @Min(1900) @Max(2100) Integer referenceYear,
    @Min(1) @Max(12) Integer referenceMonth,
    String notes
) {}
