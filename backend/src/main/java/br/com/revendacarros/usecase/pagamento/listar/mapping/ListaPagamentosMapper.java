package br.com.revendacarros.usecase.pagamento.listar.mapping;

import br.com.revendacarros.application.service.model.ResumoPagamento;
import br.com.revendacarros.usecase.pagamento.listar.contract.PagamentoItem;

public final class ListaPagamentosMapper {

    private ListaPagamentosMapper() {}

    public static PagamentoItem toItem(ResumoPagamento resumo) {
        return new PagamentoItem(
            resumo.id(),
            resumo.tipoPagamento(),
            resumo.descricao(),
            resumo.valor(),
            resumo.dataPagamento(),
            resumo.veiculoId(),
            resumo.veiculoPlaca(),
            resumo.observacoes(),
            resumo.criadoEm(),
            resumo.atualizadoEm()
        );
    }
}
