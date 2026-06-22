package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.CashBalance;
import br.com.carreselling.domain.model.CashBalanceHistory;

public interface CashBalanceRepository {

    CashBalance findCashBalance(int companyId);

    void updateCashBalance(int companyId, double amount);

    void saveCashBalanceHistory(int companyId, CashBalanceHistory history);
}
