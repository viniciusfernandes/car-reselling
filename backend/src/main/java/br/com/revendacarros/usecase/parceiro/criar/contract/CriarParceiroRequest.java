package br.com.revendacarros.usecase.parceiro.criar.contract;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CriarParceiroRequest(
    @NotBlank String nome,
    String cidade,
    String telefone,
    String email,
    BigDecimal taxaComissao
) {
}
