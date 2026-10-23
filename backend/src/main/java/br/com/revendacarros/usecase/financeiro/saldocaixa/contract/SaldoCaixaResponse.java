package br.com.revendacarros.usecase.financeiro.saldocaixa.contract;

import java.math.BigDecimal;
import java.time.Instant;

public record SaldoCaixaResponse(BigDecimal valor, Instant atualizadoEm) {
}
