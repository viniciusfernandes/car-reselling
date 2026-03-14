package br.com.carreselling.application.service;

import br.com.carreselling.application.service.VehicleSalesCalculator.TaxBreakdown;
import br.com.carreselling.application.service.model.SoldVehicle;
import br.com.carreselling.application.service.model.SoldVehiclesReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleSalesCalculatorTest {

    private VehicleSalesCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new VehicleSalesCalculator();
    }

    // -------------------------------------------------------------------------
    // calculateTaxes
    // -------------------------------------------------------------------------

    @Test
    void testCalculateTaxes_standardProfitScenario() {
        // purchase = 100,000 / selling = 120,000 → taxableMargin = 20,000
        BigDecimal sellingPrice   = new BigDecimal("120000");
        BigDecimal taxableMargin  = new BigDecimal("20000");

        TaxBreakdown result = calculator.calculateTaxes(sellingPrice, taxableMargin);

        // ICMS  = 120,000 × 0.05 × 0.12
        assertThat(result.icms()).isEqualByComparingTo("720.00");
        // PIS   = 20,000 × 0.0065
        assertThat(result.pis()).isEqualByComparingTo("130.00");
        // COFINS = 20,000 × 0.03
        assertThat(result.cofins()).isEqualByComparingTo("600.00");
        // CSLL  = 20,000 × 0.0288
        assertThat(result.csll()).isEqualByComparingTo("576.00");
        // IRPJ  = 20,000 × 0.048
        assertThat(result.irpj()).isEqualByComparingTo("960.00");
        // total = 720 + 130 + 600 + 576 + 960
        assertThat(result.totalTaxes()).isEqualByComparingTo("2986.00");
    }

    @Test
    void testCalculateTaxes_zeroMargin_onlyIcmsApplies() {
        // When there is no profit the taxable margin is 0;
        // PIS / COFINS / CSLL / IRPJ must be zero, ICMS is still due on selling price.
        BigDecimal sellingPrice  = new BigDecimal("120000");
        BigDecimal taxableMargin = BigDecimal.ZERO;

        TaxBreakdown result = calculator.calculateTaxes(sellingPrice, taxableMargin);

        assertThat(result.icms()).isEqualByComparingTo("720.00");
        assertThat(result.pis()).isEqualByComparingTo("0.00");
        assertThat(result.cofins()).isEqualByComparingTo("0.00");
        assertThat(result.csll()).isEqualByComparingTo("0.00");
        assertThat(result.irpj()).isEqualByComparingTo("0.00");
        assertThat(result.totalTaxes()).isEqualByComparingTo("720.00");
    }

    @Test
    void testCalculateTaxes_nullInputs_returnsAllZeros() {
        TaxBreakdown result = calculator.calculateTaxes(null, null);

        assertThat(result.icms()).isEqualByComparingTo("0.00");
        assertThat(result.pis()).isEqualByComparingTo("0.00");
        assertThat(result.cofins()).isEqualByComparingTo("0.00");
        assertThat(result.csll()).isEqualByComparingTo("0.00");
        assertThat(result.irpj()).isEqualByComparingTo("0.00");
        assertThat(result.totalTaxes()).isEqualByComparingTo("0.00");
    }

    // -------------------------------------------------------------------------
    // buildReport
    // -------------------------------------------------------------------------

    @Test
    void testBuildReport_standardProfitScenario() {
        SoldVehicle vehicle = new SoldVehicle(
            UUID.randomUUID(),
            "ABC1234",
            "Toyota",
            "Corolla",
            2022,
            LocalDate.of(2024, 6, 1),
            new BigDecimal("100000"),   // purchasePrice
            BigDecimal.ZERO,            // purchaseCommission
            BigDecimal.ZERO,            // freightCost
            new BigDecimal("120000"),   // sellingPrice
            BigDecimal.ZERO,            // servicesTotal
            new BigDecimal("0.05")      // saleCommissionRate
        );

        SoldVehiclesReport report = calculator.buildReport(List.of(vehicle));

        assertThat(report.totalVehiclesSold()).isEqualTo(1);
        assertThat(report.totalSoldValue()).isEqualByComparingTo("120000.00");
        assertThat(report.totalTaxesValue()).isEqualByComparingTo("2986.00");
        assertThat(report.totalServiceValue()).isEqualByComparingTo("0.00");
        assertThat(report.totalCommissionValue()).isEqualByComparingTo("0.00");
        // profit = baseProfit − taxes − freight − services − commissionIr
        //        = 20,000 − 2,986 = 17,014
        assertThat(report.profit()).isEqualByComparingTo("17014.00");
    }

    @Test
    void testBuildReport_sellingBelowPurchase_negativeProfitNoMarginTaxes() {
        // When selling price is below purchase price the taxable margin is clamped
        // to zero, so only ICMS is charged.
        SoldVehicle vehicle = new SoldVehicle(
            UUID.randomUUID(),
            "XYZ9999",
            "Honda",
            "Civic",
            2021,
            LocalDate.of(2024, 7, 15),
            new BigDecimal("120000"),   // purchasePrice
            BigDecimal.ZERO,            // purchaseCommission
            BigDecimal.ZERO,            // freightCost
            new BigDecimal("100000"),   // sellingPrice
            BigDecimal.ZERO,            // servicesTotal
            null                        // saleCommissionRate
        );

        SoldVehiclesReport report = calculator.buildReport(List.of(vehicle));

        // ICMS = 100,000 × 0.05 × 0.12 = 600.00
        assertThat(report.totalTaxesValue()).isEqualByComparingTo("600.00");
        // profit = −20,000 − 600 = −20,600
        assertThat(report.profit()).isEqualByComparingTo("-20600.00");
    }

    @Test
    void testBuildReport_emptyList_returnsZeroTotals() {
        SoldVehiclesReport report = calculator.buildReport(List.of());

        assertThat(report.totalVehiclesSold()).isZero();
        assertThat(report.totalSoldValue()).isEqualByComparingTo("0.00");
        assertThat(report.totalTaxesValue()).isEqualByComparingTo("0.00");
        assertThat(report.profit()).isEqualByComparingTo("0.00");
    }
}
