package br.com.revendacarros.domain.repository;

import br.com.revendacarros.domain.model.ServicoNoVeiculo;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServicoRepository {

    ServicoNoVeiculo saveService(int empresaId, ServicoNoVeiculo servicoNoVeiculo);

    Optional<ServicoNoVeiculo> findServicoById(int empresaId, UUID id);

    List<ServicoNoVeiculo> findServicoByVeiculoId(int empresaId, UUID veiculoId);

    ServicoNoVeiculo updateService(int empresaId, ServicoNoVeiculo servicoNoVeiculo);

    void deleteService(int empresaId, UUID id);

    BigDecimal findServicoTotalByVeiculoId(int empresaId, UUID veiculoId);

    void deleteServicosByVeiculoId(int empresaId, UUID veiculoId);

    boolean existsOpenServicoByVeiculoId(int empresaId, UUID veiculoId);
}
