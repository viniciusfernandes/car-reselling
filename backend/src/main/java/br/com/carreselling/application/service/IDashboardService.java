package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.FinancialDashboard;

import java.time.LocalDate;

public interface IDashboardService {

    FinancialDashboard getFinancialDashboard(int companyId, LocalDate startDate, LocalDate endDate);
}
