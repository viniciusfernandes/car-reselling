package br.com.carreselling.application.service;

import br.com.carreselling.domain.model.CashBalance;
import br.com.carreselling.usecase.UseCaseException;

public interface ICashBalanceService {

    CashBalance getCashBalance(int companyId);

    void updateCashBalance(int companyId, double amount, String changedBy) throws UseCaseException;
}
