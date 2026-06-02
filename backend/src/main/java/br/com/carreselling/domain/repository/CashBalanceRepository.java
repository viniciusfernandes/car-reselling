package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.CashBalance;
import br.com.carreselling.domain.model.CashBalanceHistory;

public interface CashBalanceRepository {

    CashBalance findCashBalance();

    void updateCashBalance(double amount);

    void saveCashBalanceHistory(CashBalanceHistory history);
}
