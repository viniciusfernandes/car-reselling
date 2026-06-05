package br.com.carreselling.application.service.model;

import java.math.BigDecimal;
import java.util.List;

public record FinancialDashboard(BigDecimal cashBalanceAmount,
                                 BigDecimal valorEmCaixa,
                                 BigDecimal patrimonio,
                                 int activeVehiclesCount,
                                 BigDecimal activeVehiclesTotalCost,
                                 BigDecimal lucroVendas,
                                 int totalVehiclesSold,
                                 BigDecimal lucroCompras,
                                 int totalVehiclesAcquired,
                                 BigDecimal totalPayments,
                                 List<FinancialMonthlyPoint> monthlyEvolution) {
}
