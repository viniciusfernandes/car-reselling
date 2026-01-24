package br.com.carreselling.usecase.service.list.contract;

import br.com.carreselling.domain.model.ServiceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ServiceItem(UUID id,
                          UUID vehicleId,
                          ServiceType serviceType,
                          String description,
                          BigDecimal serviceValue,
                          LocalDate performedAt) {
}
