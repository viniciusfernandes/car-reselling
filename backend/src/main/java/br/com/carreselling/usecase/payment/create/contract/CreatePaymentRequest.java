package br.com.carreselling.usecase.payment.create.contract;

import br.com.carreselling.domain.model.PaymentType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePaymentRequest(
    @NotNull PaymentType paymentType,
    String description,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotNull LocalDate paymentDate,
    String vehicleLicensePlate,
    @NotBlank String referenceMonth,
    String notes
) {}
