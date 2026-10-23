package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.model.ResumoServico;
import br.com.revendacarros.domain.model.ServicoNoVeiculo;
import br.com.revendacarros.domain.model.TipoServico;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IServicoNoVeiculoService {

    UUID addService(int empresaId,
                    UUID veiculoId,
                    TipoServico tipoServico,
                    BigDecimal valorServico,
                    String descricao,
                    LocalDate dataInicio,
                    LocalDate dataFim);

    List<ResumoServico> listarServicos(int empresaId, UUID veiculoId);

    BigDecimal totalServicos(int empresaId, UUID veiculoId);

    void updateService(int empresaId,
                       UUID veiculoId,
                       UUID servicoId,
                       TipoServico tipoServico,
                       BigDecimal valorServico,
                       String descricao,
                       LocalDate dataInicio,
                       LocalDate dataFim);

    void deleteService(int empresaId, UUID veiculoId, UUID servicoId);

    long calcularTotalDiasServico(int empresaId, List<ServicoNoVeiculo> servicos);

    double calcularTotalDiasServico(int empresaId, UUID veiculoId);
}
