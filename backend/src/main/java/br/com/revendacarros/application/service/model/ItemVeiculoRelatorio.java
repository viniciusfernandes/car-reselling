package br.com.revendacarros.application.service.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ItemVeiculoRelatorio(UUID veiculoId,
                                String placa,
                                String marca,
                                String modelo,
                                int ano,
                                LocalDate distribuidoEm,
                                BigDecimal precoCompra,
                               BigDecimal comissaoCompra,
                                BigDecimal custoTotal) {
}
