package br.com.revendacarros.application.service.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ItemVeiculoVendido(UUID veiculoId,
                              String placa,
                              String marca,
                              String modelo,
                              int ano,
                              LocalDate dataVenda,
                              BigDecimal precoCompra,
                              BigDecimal precoVenda,
                              BigDecimal totalImpostos,
                              BigDecimal totalServicos,
                              BigDecimal comissaoCompra,
                              BigDecimal lucro,
                              BigDecimal lucroAntesImpostos,
                              BigDecimal taxaComissao) {
}
