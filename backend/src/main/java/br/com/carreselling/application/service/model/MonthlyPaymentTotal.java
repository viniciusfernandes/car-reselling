package br.com.carreselling.application.service.model;

import java.math.BigDecimal;

public record MonthlyPaymentTotal(int year,
                                   int month,
                                   BigDecimal total) {
}
