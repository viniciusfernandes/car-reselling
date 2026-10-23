package br.com.revendacarros.usecase.veiculo.atualizar.contract;

import br.com.revendacarros.domain.model.OrigemFornecedor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public record AtualizarVeiculoRequest(
    @NotNull Integer ano,
    @NotBlank String cor,
    @NotBlank String modelo,
    @NotBlank String marca,
    @NotNull OrigemFornecedor origemFornecedor,
    @NotNull @PositiveOrZero BigDecimal precoCompra,
    @PositiveOrZero BigDecimal custoFrete,
    @NotNull @PositiveOrZero BigDecimal comissaoCompra,
    UUID documentoNotaFiscalCompraId,
    UUID documentoComprovantePagamentoCompraId,
    @PositiveOrZero BigDecimal valorFipe
) {
}
