package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.VehiclesTotalCost;
import br.com.carreselling.application.service.model.DistributedVehiclesFilter;
import br.com.carreselling.application.service.model.FinancialDashboard;
import br.com.carreselling.application.service.model.FinancialMonthlyPoint;
import br.com.carreselling.application.service.model.MonthlyPaymentTotal;
import br.com.carreselling.application.service.model.SoldVehicle;
import br.com.carreselling.application.service.model.SoldVehicleItem;
import br.com.carreselling.application.service.model.SoldVehiclesReport;
import br.com.carreselling.domain.repository.PaymentRepository;
import br.com.carreselling.domain.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService implements IDashboardService {

    private final VehicleRepository vehicleRepository;
    private final PaymentRepository paymentRepository;
    private final VehicleSalesCalculator salesCalculator;

    public DashboardService(VehicleRepository vehicleRepository,
                            PaymentRepository paymentRepository,
                            VehicleSalesCalculator salesCalculator) {
        this.vehicleRepository = vehicleRepository;
        this.paymentRepository = paymentRepository;
        this.salesCalculator = salesCalculator;
    }

    @Override
    public FinancialDashboard getFinancialDashboard(BigDecimal cashBase) {
        BigDecimal base = cashBase == null ? BigDecimal.ZERO : cashBase;

        List<SoldVehicle> soldVehicles = vehicleRepository.findTotalServicesFromSoldVehicles(new DistributedVehiclesFilter(null, null, null, null, null));
        SoldVehiclesReport soldReport = salesCalculator.buildReport(soldVehicles);

        VehiclesTotalCost vehicleCost = vehicleRepository.findVehicleTotalCost();
        BigDecimal totalPayments = paymentRepository.findTotalPaymentsAmount();
        List<MonthlyPaymentTotal> monthlyPayments = paymentRepository.findMonthlyPaymentTotals();

        BigDecimal lucroVendas = soldReport.profit();
        BigDecimal valorEmCaixa = base
                .add(lucroVendas)
                .subtract(vehicleCost.totalCost())
                .subtract(totalPayments);
        BigDecimal patrimonio = valorEmCaixa.add(vehicleCost.totalCost());
        BigDecimal lucroCompras = soldReport.totalCommissionValue()
                .add(vehicleCost.totalPurchaseCommission());
        int totalAcquired = vehicleCost.totalVehicles() + soldReport.totalVehiclesSold();

        List<FinancialMonthlyPoint> monthlyEvolution = buildMonthlyEvolution(
                soldReport.vehicles(), monthlyPayments);

        return new FinancialDashboard(
                valorEmCaixa,
                patrimonio,
                vehicleCost.totalVehicles(),
                vehicleCost.totalCost(),
                lucroVendas,
                soldReport.totalVehiclesSold(),
                lucroCompras,
                totalAcquired,
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

        int startIdx = Math.max(0, sortedKeys.size() - 12);
        List<String> last12 = sortedKeys.subList(startIdx, sortedKeys.size());

        List<FinancialMonthlyPoint> result = new ArrayList<>();
        for (String key : last12) {
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
