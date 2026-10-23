package br.com.revendacarros.application.service.model;

import java.math.BigDecimal;

public record ImpostosVeiculo(BigDecimal icms,
                           BigDecimal pis,
                           BigDecimal cofins,
                           BigDecimal csll,
                           BigDecimal irpj,
                           BigDecimal totalImpostos) {
}
