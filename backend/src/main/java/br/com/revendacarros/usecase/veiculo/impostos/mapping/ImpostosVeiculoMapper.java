package br.com.revendacarros.usecase.veiculo.impostos.mapping;

import br.com.revendacarros.application.service.model.ImpostosVeiculo;
import br.com.revendacarros.usecase.veiculo.impostos.contract.ImpostosVeiculoResponse;

public class ImpostosVeiculoMapper {

    private ImpostosVeiculoMapper() {
    }

    public static ImpostosVeiculoResponse toResponse(ImpostosVeiculo impostos) {
        return new ImpostosVeiculoResponse(
            impostos.icms(),
            impostos.pis(),
            impostos.cofins(),
            impostos.csll(),
            impostos.irpj(),
            impostos.totalImpostos()
        );
    }
}
