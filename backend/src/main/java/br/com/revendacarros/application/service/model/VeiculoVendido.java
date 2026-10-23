package br.com.revendacarros.application.service.model;

import java.math.BigDecimal;

public record VeiculoVendido(java.util.UUID veiculoId,
                          String placa,
                          String marca,
                          String modelo,
                          int ano,
                          java.time.LocalDate dataVenda,
                          BigDecimal precoCompra,
                          BigDecimal comissaoCompra,
                          BigDecimal custoFrete,
                          BigDecimal precoVenda,
                          BigDecimal totalServicos,
                          BigDecimal taxaComissaoVenda) {
}