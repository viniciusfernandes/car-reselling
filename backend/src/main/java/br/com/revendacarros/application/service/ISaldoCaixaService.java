package br.com.revendacarros.application.service;

import br.com.revendacarros.domain.model.SaldoCaixa;
import br.com.revendacarros.usecase.UseCaseException;

public interface ISaldoCaixaService {

    SaldoCaixa getSaldoCaixa(int empresaId);

    void updateSaldoCaixa(int empresaId, double valor, String alteradoPor) throws UseCaseException;
}
