package br.com.revendacarros.domain.repository;

import br.com.revendacarros.domain.model.HistoricoParceiro;
import java.util.List;
import java.util.UUID;

public interface HistoricoParceiroRepository {

    void saveHistorico(int empresaId, HistoricoParceiro historico);

    List<HistoricoParceiro> findHistoricoByParceiroId(int empresaId, UUID parceiroId);
}
