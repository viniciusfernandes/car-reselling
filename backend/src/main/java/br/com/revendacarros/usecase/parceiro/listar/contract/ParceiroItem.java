package br.com.revendacarros.usecase.parceiro.listar.contract;

import java.math.BigDecimal;
import java.util.UUID;

public record ParceiroItem(UUID id, String nome, String cidade, String telefone, String email, BigDecimal taxaComissao) {
}
