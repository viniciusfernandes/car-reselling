package br.com.revendacarros.usecase.veiculo.distribuicao.contract;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AtribuirParceiroRequest(@NotNull UUID parceiroId) {
}
