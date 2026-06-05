package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.SoldVehicleItem;
import br.com.carreselling.application.service.model.SoldVehicle;
import br.com.carreselling.application.service.model.SoldVehiclesReport;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class VehicleSalesCalculator {

    public SoldVehiclesReport buildSoldVehicles(List<SoldVehicle> vehicles) {
        List<SoldVehicleItem> items = new ArrayList<>();
        BigDecimal totalSold = BigDecimal.ZERO;
        BigDecimal totalTaxes = BigDecimal.ZERO;
        BigDecimal totalServices = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;
        BigDecimal totalCommissionIr = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;

        for (SoldVehicle vehicle : vehicles) {
            BigDecimal sellingPrice = vehicle.sellingPrice();
            BigDecimal servicesTotal = vehicle.servicesTotal();
            BigDecimal purchaseCommission = vehicle.purchaseCommission() == null
                ? BigDecimal.ZERO
                : vehicle.purchaseCommission();
            BigDecimal baseProfit = sellingPrice.subtract(vehicle.purchasePrice());
            BigDecimal taxableMargin = baseProfit.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : baseProfit;

            TaxBreakdown taxes = calculateTaxes(sellingPrice, taxableMargin);
            BigDecimal commissionIr = purchaseCommission
                    .multiply(TaxRate.IR_COMMISSION.value())
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal profitNoTaxes = baseProfit
                .subtract(vehicle.freightCost())
                .subtract(servicesTotal)
                .subtract(commissionIr);

            BigDecimal profit = profitNoTaxes.subtract(taxes.totalTaxes());
            items.add(new SoldVehicleItem(
                vehicle.vehicleId(),
                vehicle.licensePlate(),
                vehicle.brand(),
                vehicle.model(),
                vehicle.year(),
                vehicle.soldAt(),
                vehicle.purchasePrice(),
                sellingPrice,
                taxes.totalTaxes(),
                servicesTotal,
                purchaseCommission,
                profit,
                profitNoTaxes,
                vehicle.saleCommissionRate()
            ));
            totalSold = totalSold.add(sellingPrice);
            totalTaxes = totalTaxes.add(taxes.totalTaxes());
            totalServices = totalServices.add(servicesTotal);
            totalCommission = totalCommission.add(purchaseCommission);
            totalCommissionIr = totalCommissionIr.add(commissionIr);
            totalProfit = totalProfit.add(profit);
        }

        return new SoldVehiclesReport(
            items,
            items.size(),
            totalSold,
            totalTaxes,
            totalServices,
            totalCommission,
            totalProfit,
            totalProfit.add(totalTaxes)
        );
    }

    public TaxBreakdown calculateTaxes(BigDecimal sellingPrice, BigDecimal taxableMargin) {
        if (sellingPrice == null || taxableMargin == null) {
            return new TaxBreakdown(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
            );
        }
        BigDecimal icms = sellingPrice
                .multiply(TaxRate.ICMS_BASE.value())
                .multiply(TaxRate.ICMS.value());
        BigDecimal pis = taxableMargin.multiply(TaxRate.PIS.value());
        BigDecimal cofins = taxableMargin.multiply(TaxRate.COFINS.value());
        BigDecimal csll = taxableMargin.multiply(TaxRate.CSLL.value());
        BigDecimal irpj = taxableMargin.multiply(TaxRate.IRPJ.value());
        BigDecimal total = icms
            .add(pis)
            .add(cofins)
            .add(csll)
            .add(irpj)
            .setScale(2, RoundingMode.HALF_UP);
        return new TaxBreakdown(
            icms.setScale(2, RoundingMode.HALF_UP),
            pis.setScale(2, RoundingMode.HALF_UP),
            cofins.setScale(2, RoundingMode.HALF_UP),
            csll.setScale(2, RoundingMode.HALF_UP),
            irpj.setScale(2, RoundingMode.HALF_UP),
            total
        );
    }

    public record TaxBreakdown(BigDecimal icms,
                               BigDecimal pis,
                               BigDecimal cofins,
                               BigDecimal csll,
                               BigDecimal irpj,
                               BigDecimal totalTaxes) {
    }

}
