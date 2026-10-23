package br.com.revendacarros.usecase.servico.atualizar.contract;

import br.com.revendacarros.domain.model.TipoServico;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AtualizarServicoRequest(
    @NotNull TipoServico tipoServico,
    @NotNull @PositiveOrZero BigDecimal valorServico,
    @Size(max = 500) String descricao,
    @NotNull LocalDate dataInicio,
    LocalDate dataFim
) {
}
