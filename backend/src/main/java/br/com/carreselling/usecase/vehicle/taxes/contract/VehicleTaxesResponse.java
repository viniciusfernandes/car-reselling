package br.com.carreselling.usecase.vehicle.taxes.contract;

import java.math.BigDecimal;

public record VehicleTaxesResponse(BigDecimal icms,
                                   BigDecimal pis,
                                   BigDecimal cofins,
                                   BigDecimal csll,
                                   BigDecimal irpj,
                                   BigDecimal totalTaxes) {
}
