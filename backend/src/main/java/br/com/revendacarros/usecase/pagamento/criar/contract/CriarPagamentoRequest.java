package br.com.revendacarros.usecase.pagamento.criar.contract;

import br.com.revendacarros.domain.model.TipoPagamento;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CriarPagamentoRequest(
    @NotNull TipoPagamento tipoPagamento,
    String descricao,
    @NotNull @DecimalMin("0.01") BigDecimal valor,
    @NotNull LocalDate dataPagamento,
    String veiculoPlaca,
    String observacoes
) {}
