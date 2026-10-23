package br.com.revendacarros.usecase.marca.listar.mapping;

import br.com.revendacarros.application.service.model.ResumoMarca;
import br.com.revendacarros.usecase.marca.listar.contract.MarcaItem;

public class ListaMarcasMapper {

    private ListaMarcasMapper() {
    }

    public static MarcaItem toItem(ResumoMarca marca) {
        return new MarcaItem(marca.id(), marca.nome());
    }
}
