package br.com.carreselling.application.service.model;

import java.math.BigDecimal;
import java.util.UUID;

public record ReportVehicleItem(UUID vehicleId,
                                String licensePlate,
                                String brand,
                                String model,
                                int year,
                                BigDecimal purchasePrice,
                                BigDecimal totalCost) {
}
