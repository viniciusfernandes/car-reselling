package br.com.carreselling.usecase.vehicle.update.contract;

import br.com.carreselling.domain.model.SupplierSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateVehicleRequest(
    @NotNull Integer year,
    @NotBlank String color,
    @NotBlank String model,
    @NotBlank String brand,
    @NotNull SupplierSource supplierSource,
    @NotNull @PositiveOrZero BigDecimal purchasePrice,
    @NotNull @PositiveOrZero BigDecimal freightCost,
    @NotNull @PositiveOrZero BigDecimal purchaseCommission,
    UUID purchaseInvoiceDocumentId,
    UUID purchasePaymentReceiptDocumentId
) {
}
