package br.com.revendacarros.usecase.veiculo.listar.mapping;

import br.com.revendacarros.application.service.model.ResumoVeiculo;
import br.com.revendacarros.usecase.veiculo.listar.contract.ItemListaVeiculo;

public class ListaVeiculosMapper {

    private ListaVeiculosMapper() {
    }

    public static ItemListaVeiculo toItem(ResumoVeiculo resumo) {
        return new ItemListaVeiculo(
            resumo.id(),
            resumo.placa(),
            resumo.marca(),
            resumo.modelo(),
            resumo.ano(),
            resumo.status(),
            resumo.emServico(),
            resumo.precoVenda(),
            resumo.custoTotal(),
            resumo.margemLucro(),
            resumo.diasTempoCompra(),
            resumo.totalServicos(),
            resumo.diasEmServico()
        );
    }
}
