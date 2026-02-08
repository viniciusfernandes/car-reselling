package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.SoldVehicleItem;
import br.com.carreselling.application.service.model.SoldVehiclesReport;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VehicleSalesCalculator {

    private final BigDecimal icmsRate;
    private final BigDecimal icmsBaseRate;
    private final BigDecimal pisRate;
    private final BigDecimal cofinsRate;
    private final BigDecimal csllRate;
    private final BigDecimal irpjRate;
    private final BigDecimal irCommissionRate;

    public VehicleSalesCalculator(
        @Value("${tax.icms-rate:0.12}") BigDecimal icmsRate,
        @Value("${tax.icms-base-rate:0.05}") BigDecimal icmsBaseRate,
        @Value("${tax.pis-rate:0.0065}") BigDecimal pisRate,
        @Value("${tax.cofins-rate:0.03}") BigDecimal cofinsRate,
        @Value("${tax.csll-rate:0.0288}") BigDecimal csllRate,
        @Value("${tax.irpj-rate:0.048}") BigDecimal irpjRate,
        @Value("${tax.ir-commission-rate:0.15}") BigDecimal irCommissionRate
    ) {
        this.icmsRate = icmsRate;
        this.icmsBaseRate = icmsBaseRate;
        this.pisRate = pisRate;
        this.cofinsRate = cofinsRate;
        this.csllRate = csllRate;
        this.irpjRate = irpjRate;
        this.irCommissionRate = irCommissionRate;
    }

    public SoldVehiclesReport buildReport(List<SoldVehicleRaw> vehicles) {
        List<SoldVehicleItem> items = new ArrayList<>();
        BigDecimal totalSold = BigDecimal.ZERO;
        BigDecimal totalTaxes = BigDecimal.ZERO;
        BigDecimal totalServices = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;
        BigDecimal totalCommissionIr = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;

        for (SoldVehicleRaw vehicle : vehicles) {
            BigDecimal sellingPrice = vehicle.sellingPrice();
            BigDecimal servicesTotal = vehicle.servicesTotal();
            BigDecimal purchaseCommission = vehicle.purchaseCommission() == null
                ? BigDecimal.ZERO
                : vehicle.purchaseCommission();
            BigDecimal baseProfit = sellingPrice.subtract(vehicle.purchasePrice());
            BigDecimal taxableMargin = baseProfit.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : baseProfit;

            TaxBreakdown taxes = calculateTaxes(sellingPrice, taxableMargin);
            BigDecimal commissionIr = purchaseCommission
                .multiply(irCommissionRate)
                .setScale(2, RoundingMode.HALF_UP);
            BigDecimal vehicleProfit = baseProfit
                .subtract(taxes.totalTaxes())
                .subtract(vehicle.freightCost())
                .subtract(servicesTotal)
                .subtract(commissionIr);
            items.add(new SoldVehicleItem(
                vehicle.vehicleId(),
                vehicle.licensePlate(),
                vehicle.brand(),
                vehicle.model(),
                vehicle.year(),
                vehicle.soldAt(),
                sellingPrice,
                taxes.totalTaxes(),
                servicesTotal,
                purchaseCommission
            ));
            totalSold = totalSold.add(sellingPrice);
            totalTaxes = totalTaxes.add(taxes.totalTaxes());
            totalServices = totalServices.add(servicesTotal);
            totalCommission = totalCommission.add(purchaseCommission);
            totalCommissionIr = totalCommissionIr.add(commissionIr);
            totalProfit = totalProfit.add(vehicleProfit);
        }

        return new SoldVehiclesReport(
            items,
            items.size(),
            totalSold,
            totalTaxes,
            totalServices,
            totalCommission,
            totalProfit
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
            .multiply(icmsBaseRate)
            .multiply(icmsRate);
        BigDecimal pis = taxableMargin.multiply(pisRate);
        BigDecimal cofins = taxableMargin.multiply(cofinsRate);
        BigDecimal csll = taxableMargin.multiply(csllRate);
        BigDecimal irpj = taxableMargin.multiply(irpjRate);
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

    public record SoldVehicleRaw(java.util.UUID vehicleId,
                                 String licensePlate,
                                 String brand,
                                 String model,
                                 int year,
                                 java.time.LocalDate soldAt,
                                 BigDecimal purchasePrice,
                                 BigDecimal purchaseCommission,
                                 BigDecimal freightCost,
                                 BigDecimal sellingPrice,
                                 BigDecimal servicesTotal) {
    }
}
