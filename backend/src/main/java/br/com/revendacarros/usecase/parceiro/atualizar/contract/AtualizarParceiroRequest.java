package br.com.revendacarros.usecase.parceiro.atualizar.contract;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record AtualizarParceiroRequest(
    @NotBlank String nome,
    String cidade,
    String telefone,
    String email,
    @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal taxaComissao
) {
}
