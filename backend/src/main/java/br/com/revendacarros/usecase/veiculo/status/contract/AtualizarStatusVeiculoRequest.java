package br.com.revendacarros.usecase.veiculo.status.contract;

import br.com.revendacarros.domain.model.StatusVeiculo;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AtualizarStatusVeiculoRequest(@NotNull StatusVeiculo status, UUID parceiroAtribuidoId) {
}
