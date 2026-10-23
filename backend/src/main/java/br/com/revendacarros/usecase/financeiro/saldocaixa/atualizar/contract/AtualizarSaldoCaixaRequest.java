package br.com.revendacarros.usecase.financeiro.saldocaixa.atualizar.contract;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AtualizarSaldoCaixaRequest(
        @NotNull
        @DecimalMin(value = "0", message = "Valor deve ser maior ou igual a zero")
        Double valor
) {
}
