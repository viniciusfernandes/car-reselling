package br.com.revendacarros.usecase.pagamento.listar.contract;

import br.com.revendacarros.domain.model.TipoPagamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PagamentoItem(
    UUID id,
    TipoPagamento tipoPagamento,
    String descricao,
    BigDecimal valor,
    LocalDate dataPagamento,
    UUID veiculoId,
    String veiculoPlaca,
    String observacoes,
    Instant criadoEm,
    Instant atualizadoEm
) {}
