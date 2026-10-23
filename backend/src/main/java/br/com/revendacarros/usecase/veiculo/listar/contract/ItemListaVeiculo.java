package br.com.revendacarros.usecase.veiculo.listar.contract;

import br.com.revendacarros.domain.model.StatusVeiculo;
import java.math.BigDecimal;
import java.util.UUID;

public record ItemListaVeiculo(UUID id,
                              String placa,
                              String marca,
                              String modelo,
                              int ano,
                              StatusVeiculo status,
                              boolean emServico,
                              BigDecimal precoVenda,
                              BigDecimal custoTotal,
                              BigDecimal margemLucro,
                              Integer diasTempoCompra,
                              BigDecimal totalServicos,
                              double diasEmServico) {
}
