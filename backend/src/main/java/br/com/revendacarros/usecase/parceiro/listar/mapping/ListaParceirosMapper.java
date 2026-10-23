package br.com.revendacarros.usecase.parceiro.listar.mapping;

import br.com.revendacarros.application.service.model.ResumoParceiro;
import br.com.revendacarros.usecase.parceiro.listar.contract.ParceiroItem;

public class ListaParceirosMapper {

    private ListaParceirosMapper() {
    }

    public static ParceiroItem toItem(ResumoParceiro resumo) {
        return new ParceiroItem(resumo.id(), resumo.nome(), resumo.cidade(),
            resumo.telefone(), resumo.email(), resumo.taxaComissao());
    }
}
