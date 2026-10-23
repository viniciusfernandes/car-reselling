package br.com.revendacarros.usecase.servico.listar.contract;

import java.math.BigDecimal;
import java.util.List;

public record ListaServicosResponse(List<ServicoItem> servicos, BigDecimal total) {
}
