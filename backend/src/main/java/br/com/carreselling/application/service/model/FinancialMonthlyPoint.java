package br.com.carreselling.application.service.model;

import java.math.BigDecimal;

public record FinancialMonthlyPoint(int year,
                                    int month,
                                    BigDecimal salesProfit,
                                    BigDecimal expenses,
                                    BigDecimal net) {
}
