package br.com.carreselling.usecase.vehicle.create.contract;

import br.com.carreselling.domain.model.SupplierSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateVehicleRequest(
    @NotBlank @Size(max = 16) String licensePlate,
    @Size(max = 32) String renavam,
    @Size(max = 64) String vin,
    @NotNull Integer year,
    @NotBlank String color,
    @NotBlank String model,
    @NotBlank String brand,
    @NotNull SupplierSource supplierSource,
    @NotNull @PositiveOrZero BigDecimal purchasePrice,
    @PositiveOrZero BigDecimal freightCost
) {
}
