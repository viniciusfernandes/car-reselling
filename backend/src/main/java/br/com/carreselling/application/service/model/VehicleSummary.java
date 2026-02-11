package br.com.carreselling.application.service.model;

import br.com.carreselling.domain.model.VehicleStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record VehicleSummary(UUID id,
                             String licensePlate,
                             String brand,
                             String model,
                             int year,
                             VehicleStatus status,
                             BigDecimal purchasePrice,
                             BigDecimal purchaseCommission,
                             BigDecimal servicesTotal,
                             BigDecimal totalCost,
                             String assignedPartnerName,
                             int yardDays) {
}
