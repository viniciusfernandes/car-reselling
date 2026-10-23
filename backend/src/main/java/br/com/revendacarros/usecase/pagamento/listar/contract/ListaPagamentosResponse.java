package br.com.revendacarros.usecase.pagamento.listar.contract;

import java.util.List;

public record ListaPagamentosResponse(List<PagamentoItem> pagamentos) {}
