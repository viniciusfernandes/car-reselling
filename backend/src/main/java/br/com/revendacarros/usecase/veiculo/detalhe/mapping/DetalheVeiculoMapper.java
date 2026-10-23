package br.com.revendacarros.usecase.veiculo.detalhe.mapping;

import br.com.revendacarros.application.service.model.DetalheVeiculo;
import br.com.revendacarros.usecase.veiculo.detalhe.contract.DetalheVeiculoResponse;

public class DetalheVeiculoMapper {

    private DetalheVeiculoMapper() {
    }

    public static DetalheVeiculoResponse toResponse(DetalheVeiculo detail) {
        return new DetalheVeiculoResponse(
            detail.id(),
            detail.placa(),
            detail.renavam(),
            detail.chassi(),
            detail.ano(),
            detail.cor(),
            detail.modelo(),
            detail.marca(),
            detail.origemFornecedor(),
            detail.precoCompra(),
            detail.custoFrete(),
            detail.comissaoCompra(),
            detail.precoVenda(),
            detail.valorFipe(),
            detail.documentoNotaFiscalCompraId(),
            detail.documentoComprovantePagamentoCompraId(),
            detail.status(),
            detail.emServico(),
            detail.parceiroAtribuidoId(),
            detail.nomeParceiroAtribuido(),
            detail.totalServicos(),
            detail.custoTotal(),
            detail.documentosCount(),
            detail.criadoEm(),
            detail.atualizadoEm(),
            detail.distribuidoEm()
        );
    }
}
