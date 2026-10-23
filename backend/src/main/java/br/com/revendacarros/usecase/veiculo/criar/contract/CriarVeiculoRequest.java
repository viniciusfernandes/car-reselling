package br.com.revendacarros.usecase.veiculo.criar.contract;

import br.com.revendacarros.domain.model.OrigemFornecedor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CriarVeiculoRequest(
    @NotBlank @Size(max = 16) String placa,
    @Size(max = 32) String renavam,
    @Size(max = 64) String chassi,
    @NotNull Integer ano,
    @NotBlank String cor,
    @NotBlank String modelo,
    @NotBlank String marca,
    @NotNull OrigemFornecedor origemFornecedor,
    @NotNull @PositiveOrZero BigDecimal precoCompra,
    @PositiveOrZero BigDecimal custoFrete,
    @NotNull @PositiveOrZero BigDecimal comissaoCompra,
    @PositiveOrZero BigDecimal valorFipe
) {
}
