package br.com.revendacarros.usecase.cor.listar.mapping;

import br.com.revendacarros.application.service.model.ResumoCor;
import br.com.revendacarros.usecase.cor.listar.contract.CorItem;

public class ListaCoresMapper {

    private ListaCoresMapper() {
    }

    public static CorItem toItem(ResumoCor cor) {
        return new CorItem(cor.id(), cor.nome());
    }
}
