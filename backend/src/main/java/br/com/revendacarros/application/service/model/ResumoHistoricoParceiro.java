package br.com.revendacarros.application.service.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ResumoHistoricoParceiro(
    UUID id,
    UUID parceiroId,
    String nome,
    String cidade,
    String telefone,
    String email,
    BigDecimal taxaComissao,
    Instant alteradoEm,
    String alteradoPor
) {
}
