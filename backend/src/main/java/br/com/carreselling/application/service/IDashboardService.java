package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.FinancialDashboard;

import java.math.BigDecimal;

public interface IDashboardService {

    FinancialDashboard getFinancialDashboard(BigDecimal cashBase);
}
