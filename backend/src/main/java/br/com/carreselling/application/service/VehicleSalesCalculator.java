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

    public VehicleSalesCalculator(
        @Value("${tax.icms-rate:0.12}") BigDecimal icmsRate,
        @Value("${tax.icms-base-rate:0.05}") BigDecimal icmsBaseRate,
        @Value("${tax.pis-rate:0.0065}") BigDecimal pisRate,
        @Value("${tax.cofins-rate:0.03}") BigDecimal cofinsRate,
        @Value("${tax.csll-rate:0.0288}") BigDecimal csllRate,
        @Value("${tax.irpj-rate:0.048}") BigDecimal irpjRate
    ) {
        this.icmsRate = icmsRate;
        this.icmsBaseRate = icmsBaseRate;
        this.pisRate = pisRate;
        this.cofinsRate = cofinsRate;
        this.csllRate = csllRate;
        this.irpjRate = irpjRate;
    }

    public SoldVehiclesReport buildReport(List<SoldVehicleRaw> vehicles) {
        List<SoldVehicleItem> items = new ArrayList<>();
        BigDecimal totalSold = BigDecimal.ZERO;
        BigDecimal totalTaxes = BigDecimal.ZERO;
        BigDecimal totalServices = BigDecimal.ZERO;

        for (SoldVehicleRaw vehicle : vehicles) {
            BigDecimal sellingPrice = vehicle.sellingPrice();
            BigDecimal servicesTotal = vehicle.servicesTotal();
            BigDecimal vehicleCost = vehicle.purchasePrice()
                .add(vehicle.freightCost())
                .add(servicesTotal);
            BigDecimal margin = sellingPrice.subtract(vehicleCost);
            BigDecimal taxableMargin = margin.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : margin;

            BigDecimal icms = sellingPrice
                .multiply(icmsBaseRate)
                .multiply(icmsRate);
            BigDecimal pis = taxableMargin.multiply(pisRate);
            BigDecimal cofins = taxableMargin.multiply(cofinsRate);
            BigDecimal csll = taxableMargin.multiply(csllRate);
            BigDecimal irpj = taxableMargin.multiply(irpjRate);
            BigDecimal taxes = icms
                .add(pis)
                .add(cofins)
                .add(csll)
                .add(irpj)
                .setScale(2, RoundingMode.HALF_UP);
            items.add(new SoldVehicleItem(
                vehicle.vehicleId(),
                vehicle.licensePlate(),
                vehicle.brand(),
                vehicle.model(),
                vehicle.year(),
                sellingPrice,
                taxes,
                servicesTotal
            ));
            totalSold = totalSold.add(sellingPrice);
            totalTaxes = totalTaxes.add(taxes);
            totalServices = totalServices.add(servicesTotal);
        }

        BigDecimal profit = totalSold.subtract(totalTaxes).subtract(totalServices);

        return new SoldVehiclesReport(
            items,
            items.size(),
            totalSold,
            totalTaxes,
            totalServices,
            profit
        );
    }

    public record SoldVehicleRaw(java.util.UUID vehicleId,
                                 String licensePlate,
                                 String brand,
                                 String model,
                                 int year,
                                 BigDecimal purchasePrice,
                                 BigDecimal freightCost,
                                 BigDecimal sellingPrice,
                                 BigDecimal servicesTotal) {
    }
}
