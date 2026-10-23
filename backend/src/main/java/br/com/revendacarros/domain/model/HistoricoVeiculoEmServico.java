package br.com.revendacarros.domain.model;

import java.time.Instant;
import java.util.UUID;

public record HistoricoVeiculoEmServico(UUID id, int empresaId, UUID veiculoId, boolean emServico, Instant alteradoEm) {
}
