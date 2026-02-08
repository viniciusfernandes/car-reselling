package br.com.carreselling.application.service.model;

import java.math.BigDecimal;

public record VehicleTaxes(BigDecimal icms,
                           BigDecimal pis,
                           BigDecimal cofins,
                           BigDecimal csll,
                           BigDecimal irpj,
                           BigDecimal totalTaxes) {
}
