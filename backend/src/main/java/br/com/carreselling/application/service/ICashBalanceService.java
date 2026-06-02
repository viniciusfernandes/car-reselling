package br.com.carreselling.application.service;

import br.com.carreselling.domain.model.CashBalance;
import br.com.carreselling.usecase.UseCaseException;

public interface ICashBalanceService {

    CashBalance getCashBalance();

    void updateCashBalance(double amount, String changedBy) throws UseCaseException;
}
