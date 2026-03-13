package br.com.carreselling.application.service.model;

import java.math.BigDecimal;

public record SoldVehicle(java.util.UUID vehicleId,
                          String licensePlate,
                          String brand,
                          String model,
                          int year,
                          java.time.LocalDate soldAt,
                          BigDecimal purchasePrice,
                          BigDecimal purchaseCommission,
                          BigDecimal freightCost,
                          BigDecimal sellingPrice,
                          BigDecimal servicesTotal,
                          BigDecimal saleCommissionRate) {
}