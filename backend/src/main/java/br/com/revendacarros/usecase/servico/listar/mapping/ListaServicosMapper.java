package br.com.revendacarros.usecase.servico.listar.mapping;

import br.com.revendacarros.application.service.model.ResumoServico;
import br.com.revendacarros.usecase.servico.listar.contract.ServicoItem;

public class ListaServicosMapper {

    private ListaServicosMapper() {
    }

    public static ServicoItem toItem(ResumoServico resumo) {
        return new ServicoItem(
            resumo.id(),
            resumo.veiculoId(),
            resumo.tipoServico(),
            resumo.descricao(),
            resumo.valorServico(),
            resumo.dataInicio(),
            resumo.dataFim()
        );
    }
}
