package br.com.carreselling.usecase.service.update.contract;

import br.com.carreselling.domain.model.ServiceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateServiceRequest(
    @NotNull ServiceType serviceType,
    @NotNull @PositiveOrZero BigDecimal serviceValue,
    @Size(max = 500) String description,
    LocalDate performedAt
) {
}
