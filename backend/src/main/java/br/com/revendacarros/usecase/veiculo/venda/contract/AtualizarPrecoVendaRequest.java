package br.com.revendacarros.usecase.veiculo.venda.contract;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record AtualizarPrecoVendaRequest(@NotNull @Positive BigDecimal precoVenda) {
}
