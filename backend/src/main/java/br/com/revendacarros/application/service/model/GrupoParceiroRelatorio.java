package br.com.revendacarros.application.service.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record GrupoParceiroRelatorio(UUID parceiroId,
                                 String nomeParceiro,
                                 List<ItemVeiculoRelatorio> veiculos,
                                 BigDecimal valorTotalVeiculosParceiro,
                                 int quantidadeVeiculosParceiro) {
}
