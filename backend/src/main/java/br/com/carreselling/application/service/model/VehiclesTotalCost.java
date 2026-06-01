package br.com.carreselling.application.service.model;

import java.math.BigDecimal;

public record VehiclesTotalCost(int totalVehicles,
                                BigDecimal totalCost,
                                BigDecimal totalPurchaseCommission) {
}
