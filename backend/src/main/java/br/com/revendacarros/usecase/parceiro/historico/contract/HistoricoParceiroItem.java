package br.com.revendacarros.usecase.parceiro.historico.contract;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record HistoricoParceiroItem(
    UUID id,
    String nome,
    String cidade,
    String telefone,
    String email,
    BigDecimal taxaComissao,
    Instant alteradoEm,
    String alteradoPor
) {
}
