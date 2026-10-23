package br.com.revendacarros.domain.repository;

import br.com.revendacarros.domain.model.HistoricoVeiculoEmServico;

import java.util.List;
import java.util.UUID;

public interface HistoricoVeiculoEmServicoRepository {

    void save(int empresaId, HistoricoVeiculoEmServico historico);

    List<HistoricoVeiculoEmServico> findByVeiculoId(int empresaId, UUID veiculoId);

    void deleteByVeiculoId(int empresaId, UUID veiculoId);
}
