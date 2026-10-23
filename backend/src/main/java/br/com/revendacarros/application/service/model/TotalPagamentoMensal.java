package br.com.revendacarros.application.service.model;

import java.math.BigDecimal;

public record TotalPagamentoMensal(int ano,
                                   int mes,
                                   BigDecimal total) {
}
