package br.com.revendacarros.usecase.pagamento.atualizar.contract;

import br.com.revendacarros.domain.model.TipoPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AtualizarPagamentoRequest(
    @NotNull TipoPagamento tipoPagamento,
    String descricao,
    @NotNull @DecimalMin("0.01") BigDecimal valor,
    @NotNull LocalDate dataPagamento,
    String veiculoPlaca,
    String observacoes
) {}
