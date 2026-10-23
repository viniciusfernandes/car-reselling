package br.com.revendacarros.usecase.servico.listar.contract;

import br.com.revendacarros.domain.model.TipoServico;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ServicoItem(UUID id,
                          UUID veiculoId,
                          TipoServico tipoServico,
                          String descricao,
                          BigDecimal valorServico,
                          LocalDate dataInicio,
                          LocalDate dataFim) {
}
