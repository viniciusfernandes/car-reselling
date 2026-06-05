package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.*;
import br.com.carreselling.domain.repository.CashBalanceRepository;
import br.com.carreselling.domain.repository.PaymentRepository;
import br.com.carreselling.domain.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService implements IDashboardService {

    private final VehicleRepository vehicleRepository;
    private final PaymentRepository paymentRepository;
    private final VehicleSalesCalculator salesCalculator;
    private final CashBalanceRepository cashBalanceRepository;

    public DashboardService(VehicleRepository vehicleRepository,
                            PaymentRepository paymentRepository,
                            VehicleSalesCalculator salesCalculator,
                            CashBalanceRepository cashBalanceRepository) {
        this.vehicleRepository = vehicleRepository;
        this.paymentRepository = paymentRepository;
        this.salesCalculator = salesCalculator;
        this.cashBalanceRepository = cashBalanceRepository;
    }

    @Override
    public FinancialDashboard getFinancialDashboard(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        LocalDate start = startDate != null ? startDate : today.minusMonths(12).withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : today;

        BigDecimal cashBalance = cashBalanceRepository.findCashBalance().amount;

        List<SoldVehicle> soldVehicles = vehicleRepository.findTotalServicesFromSoldVehicles(
                new DistributedVehiclesFilter(start, end, null, null, null));
        SoldVehiclesReport soldReport = salesCalculator.buildReport(soldVehicles);

        VehiclesTotalCost vehicleCost = vehicleRepository.findVehicleTotalCost();
        BigDecimal totalPayments = paymentRepository.findTotalPaymentsAmount(start, end);
        List<MonthlyPaymentTotal> monthlyPayments = paymentRepository.findMonthlyPaymentTotals(start, end);

        BigDecimal lucroVendas = soldReport.profit();
        BigDecimal lucroVendasSemImpostos = soldReport.profitBeforeTaxes();
        BigDecimal valorEmCaixa = cashBalance
                .add(lucroVendas)
                .subtract(vehicleCost.totalCost())
                .subtract(totalPayments);
        BigDecimal patrimonio = cashBalance.add(vehicleCost.totalPurchasePrice());
        BigDecimal lucroCompras = soldReport.totalCommissionValue()
                .add(vehicleCost.totalPurchaseCommission());
        int totalAcquired = vehicleCost.totalVehicles() + soldReport.totalVehiclesSold();

        List<FinancialMonthlyPoint> monthlyEvolution = buildMonthlyEvolution(
                soldReport.vehicles(), monthlyPayments);

        return new FinancialDashboard(
                cashBalance,
                valorEmCaixa,
                patrimonio,
                vehicleCost.totalVehicles(),
                vehicleCost.totalPurchasePrice(),
                lucroVendas,
                lucroVendasSemImpostos,
                soldReport.totalVehiclesSold(),
                lucroCompras,
                totalAcquired,
                totalPayments,
                monthlyEvolution
        );
    }

    private List<FinancialMonthlyPoint> buildMonthlyEvolution(List<SoldVehicleItem> soldItems,
                                                              List<MonthlyPaymentTotal> monthlyPayments) {
        Map<String, BigDecimal> salesMap = new LinkedHashMap<>();
        for (SoldVehicleItem item : soldItems) {
            if (item.soldAt() == null) {
                continue;
            }
            String key = item.soldAt().getYear() + "-" + String.format("%02d", item.soldAt().getMonthValue());
            salesMap.merge(key, item.profit(), BigDecimal::add);
        }

        Map<String, BigDecimal> expensesMap = new LinkedHashMap<>();
        for (MonthlyPaymentTotal monthly : monthlyPayments) {
            String key = monthly.year() + "-" + String.format("%02d", monthly.month());
            expensesMap.put(key, monthly.total());
        }

        Map<String, BigDecimal[]> combined = new LinkedHashMap<>();
        for (String key : salesMap.keySet()) {
            combined.computeIfAbsent(key, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO})[0] = salesMap.get(key);
        }
        for (String key : expensesMap.keySet()) {
            combined.computeIfAbsent(key, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO})[1] = expensesMap.get(key);
        }

        List<String> sortedKeys = new ArrayList<>(combined.keySet());
        sortedKeys.sort(String::compareTo);

        List<FinancialMonthlyPoint> result = new ArrayList<>();
        for (String key : sortedKeys) {
            String[] parts = key.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            BigDecimal[] values = combined.get(key);
            BigDecimal profit = values[0];
            BigDecimal expenses = values[1];
            result.add(new FinancialMonthlyPoint(year, month, profit, expenses, profit.subtract(expenses)));
        }
        return result;
    }
}
