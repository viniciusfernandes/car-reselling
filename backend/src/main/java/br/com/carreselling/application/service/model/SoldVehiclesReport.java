package br.com.carreselling.application.service.model;

import java.math.BigDecimal;
import java.util.List;

public record SoldVehiclesReport(List<SoldVehicleItem> vehicles,
                                 int totalVehiclesSold,
                                 BigDecimal totalSoldValue,
                                 BigDecimal totalTaxesValue,
                                 BigDecimal totalServiceValue,
                                 BigDecimal totalCommissionValue,
                                 BigDecimal profit) {
}
