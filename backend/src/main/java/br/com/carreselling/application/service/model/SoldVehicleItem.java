package br.com.carreselling.application.service.model;

import java.math.BigDecimal;
import java.util.UUID;

public record SoldVehicleItem(UUID vehicleId,
                              String licensePlate,
                              String brand,
                              String model,
                              int year,
                              BigDecimal sellingPrice,
                              BigDecimal totalTaxes,
                              BigDecimal servicesTotal) {
}
