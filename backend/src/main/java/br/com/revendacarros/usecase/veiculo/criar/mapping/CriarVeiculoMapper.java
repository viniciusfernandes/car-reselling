package br.com.revendacarros.usecase.veiculo.criar.mapping;

import br.com.revendacarros.common.UuidGenerator;
import br.com.revendacarros.domain.model.StatusVeiculo;
import br.com.revendacarros.domain.model.Veiculo;
import br.com.revendacarros.usecase.veiculo.criar.contract.CriarVeiculoRequest;
import java.time.Instant;

public class CriarVeiculoMapper {

    private CriarVeiculoMapper() {
    }

    public static Veiculo toDomain(int empresaId, CriarVeiculoRequest request) {
        Instant now = Instant.now();
        return new Veiculo(
            UuidGenerator.generate(),
            empresaId,
            request.placa(),
            request.renavam(),
            request.chassi(),
            request.ano(),
            request.cor(),
            request.modelo(),
            request.marca(),
            null,
            null,
            request.origemFornecedor(),
            request.precoCompra(),
            request.custoFrete(),
            request.comissaoCompra(),
            null,
            request.valorFipe(),
            null,
            null,
            StatusVeiculo.NO_PATIO,
            null,
            null,
            null,
            null,
            now,
            now
        );
    }
}
