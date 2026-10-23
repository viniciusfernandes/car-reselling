package br.com.revendacarros.application.service.model;

import br.com.revendacarros.domain.model.TipoServico;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ResumoServico(UUID id,
                             UUID veiculoId,
                             TipoServico tipoServico,
                             String descricao,
                             BigDecimal valorServico,
                             LocalDate dataInicio,
                             LocalDate dataFim) {
}
