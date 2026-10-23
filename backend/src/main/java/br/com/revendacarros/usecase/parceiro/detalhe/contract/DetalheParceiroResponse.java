package br.com.revendacarros.usecase.parceiro.detalhe.contract;

import java.math.BigDecimal;
import java.util.UUID;

public record DetalheParceiroResponse(
    UUID id,
    String nome,
    String cidade,
    String telefone,
    String email,
    BigDecimal taxaComissao
) {
}
