package br.com.carreselling.application.service;

import java.math.BigDecimal;

public enum TaxRate {

    ICMS(new BigDecimal("0.12")),
    ICMS_BASE(new BigDecimal("0.05")),
    PIS(new BigDecimal("0.0065")),
    COFINS(new BigDecimal("0.03")),
    CSLL(new BigDecimal("0.0288")),
    IRPJ(new BigDecimal("0.048")),
    IR_COMMISSION(new BigDecimal("0.15"));

    private final BigDecimal value;

    TaxRate(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal value() {
        return value;
    }
}
