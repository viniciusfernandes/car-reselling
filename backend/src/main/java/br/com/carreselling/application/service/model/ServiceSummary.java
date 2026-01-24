package br.com.carreselling.application.service.model;

import br.com.carreselling.domain.model.ServiceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ServiceSummary(UUID id,
                             UUID vehicleId,
                             ServiceType serviceType,
                             String description,
                             BigDecimal serviceValue,
                             LocalDate performedAt) {
}
