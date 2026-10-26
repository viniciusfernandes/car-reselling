package br.com.revendacarros.domain.repository;

import br.com.revendacarros.domain.model.SaldoCaixa;
import br.com.revendacarros.domain.model.HistoricoSaldoCaixa;

public interface SaldoCaixaRepository {

    SaldoCaixa findSaldoCaixa(int empresaId);

    void updateSaldoCaixa(int empresaId, double valor);

    boolean  existsSaldoCaixa(int empresaId);

    void createSaldoCaixa(int empresaId, double valor);

    void saveHistoricoSaldoCaixa(int empresaId, HistoricoSaldoCaixa historico);
}
