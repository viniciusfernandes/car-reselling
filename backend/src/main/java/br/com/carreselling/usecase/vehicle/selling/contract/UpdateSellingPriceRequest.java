package br.com.carreselling.usecase.vehicle.selling.contract;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record UpdateSellingPriceRequest(@NotNull @Positive BigDecimal sellingPrice) {
}
