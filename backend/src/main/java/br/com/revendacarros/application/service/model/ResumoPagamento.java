package br.com.revendacarros.application.service.model;

import br.com.revendacarros.domain.model.TipoPagamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ResumoPagamento(
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
