package br.com.revendacarros.application.service;

import br.com.revendacarros.domain.model.SaldoCaixa;
import br.com.revendacarros.domain.model.HistoricoSaldoCaixa;
import br.com.revendacarros.domain.repository.SaldoCaixaRepository;
import br.com.revendacarros.usecase.UseCaseException;
import org.springframework.stereotype.Service;

@Service
public class SaldoCaixaService implements ISaldoCaixaService {

    private final SaldoCaixaRepository saldoCaixaRepository;

    public SaldoCaixaService(SaldoCaixaRepository saldoCaixaRepository) {
        this.saldoCaixaRepository = saldoCaixaRepository;
    }

    @Override
    public SaldoCaixa getSaldoCaixa(int empresaId) {
        return saldoCaixaRepository.findSaldoCaixa(empresaId);
    }

    @Override
    public void updateSaldoCaixa(int empresaId, double valor, String alteradoPor) throws UseCaseException {
        if (alteradoPor == null || alteradoPor.isBlank()) {
            throw new UseCaseException("Falha ao atualizar o saldo do caixa. O autor não pode ser nulo ou vazio.");
        }
        saldoCaixaRepository.updateSaldoCaixa(empresaId, valor);
        HistoricoSaldoCaixa historico = new HistoricoSaldoCaixa(empresaId, valor, alteradoPor);
        saldoCaixaRepository.saveHistoricoSaldoCaixa(empresaId, historico);
    }
}
