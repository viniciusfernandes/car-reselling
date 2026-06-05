package br.com.carreselling.usecase.vehicle.list.contract;

import br.com.carreselling.domain.model.VehicleStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record VehicleListItem(UUID id,
                              String licensePlate,
                              String brand,
                              String model,
                              int year,
                              VehicleStatus status,
                              boolean onService,
                              BigDecimal sellingPrice,
                              BigDecimal totalCost,
                              BigDecimal profitMargin,
                              Integer purchaseTimeDays,
                              BigDecimal servicesTotal,
                              double daysOnService) {
}
