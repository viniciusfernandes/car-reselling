package br.com.revendacarros.usecase.marca.modelo.listar.mapping;

import br.com.revendacarros.application.service.model.ResumoModeloVeiculo;
import br.com.revendacarros.usecase.marca.modelo.listar.contract.ModeloVeiculoItem;

public class ListaModelosVeiculoMapper {

    private ListaModelosVeiculoMapper() {
    }

    public static ModeloVeiculoItem toItem(ResumoModeloVeiculo modelo) {
        return new ModeloVeiculoItem(modelo.id(), modelo.nome());
    }
}
