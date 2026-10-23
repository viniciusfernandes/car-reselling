package br.com.revendacarros.usecase.parceiro.historico.contract;

import java.util.List;

public record HistoricoParceiroResponse(List<HistoricoParceiroItem> historico) {
}
