package br.com.carreselling.application.service;

import br.com.carreselling.domain.model.CashBalance;
import br.com.carreselling.domain.model.CashBalanceHistory;
import br.com.carreselling.domain.repository.CashBalanceRepository;
import br.com.carreselling.usecase.UseCaseException;
import org.springframework.stereotype.Service;

@Service
public class CashBalanceService implements ICashBalanceService {

    private final CashBalanceRepository cashBalanceRepository;

    public CashBalanceService(CashBalanceRepository cashBalanceRepository) {
        this.cashBalanceRepository = cashBalanceRepository;
    }

    @Override
    public CashBalance getCashBalance(int companyId) {
        return cashBalanceRepository.findCashBalance(companyId);
    }

    @Override
    public void updateCashBalance(int companyId, double amount, String changedBy) throws UseCaseException {
        if (changedBy == null || changedBy.isBlank()) {
            throw new UseCaseException("Failure on updating cash balance. Author must no be null or empty.");
        }
        cashBalanceRepository.updateCashBalance(companyId, amount);
        CashBalanceHistory history = new CashBalanceHistory(companyId, amount, changedBy);
        cashBalanceRepository.saveCashBalanceHistory(companyId, history);
    }
}
