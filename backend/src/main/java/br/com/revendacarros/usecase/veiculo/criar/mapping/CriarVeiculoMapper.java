package br.com.revendacarros.usecase.veiculo.criar.mapping;

import br.com.revendacarros.usecase.veiculo.criar.contract.CriarVeiculoRequest;

public class CriarVeiculoMapper {

    private CriarVeiculoMapper() {
    }

    public static CriarVeiculoRequest normalize(CriarVeiculoRequest request) {
        if (request == null || request.placa() == null) {
            return request;
        }
        return new CriarVeiculoRequest(
            request.placa().trim().toUpperCase(),
            request.renavam(),
            request.chassi(),
            request.ano(),
            request.cor(),
            request.modelo(),
            request.marca(),
            request.origemFornecedor(),
            request.precoCompra(),
            request.custoFrete(),
            request.comissaoCompra(),
            request.valorFipe()
        );
    }
}
