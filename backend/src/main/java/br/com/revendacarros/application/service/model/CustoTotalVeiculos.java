package br.com.revendacarros.application.service.model;

import java.math.BigDecimal;

public record CustoTotalVeiculos(int totalVeiculos,
                                BigDecimal custoTotal,
                                BigDecimal totalPrecoCompra,
                                BigDecimal totalComissaoCompra) {
}
