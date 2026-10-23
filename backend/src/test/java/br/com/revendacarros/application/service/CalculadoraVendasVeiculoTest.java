package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.CalculadoraVendasVeiculo.ImpostoBreakdown;
import br.com.revendacarros.application.service.model.VeiculoVendido;
import br.com.revendacarros.application.service.model.RelatorioVeiculosVendidos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraVendasVeiculoTest {

    private CalculadoraVendasVeiculo calculator;

    @BeforeEach
    void setUp() {
        calculator = new CalculadoraVendasVeiculo();
    }

    // -------------------------------------------------------------------------
    // calcularImpostos
    // -------------------------------------------------------------------------

    @Test
    void testCalculateTaxes_standardProfitScenario() {
        // compra = 100,000 / venda = 120,000 → taxableMargem = 20,000
        BigDecimal precoVenda   = new BigDecimal("120000");
        BigDecimal taxableMargem  = new BigDecimal("20000");

        ImpostoBreakdown result = calculator.calcularImpostos(precoVenda, taxableMargem);

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
        assertThat(result.totalImpostos()).isEqualByComparingTo("2986.00");
    }

    @Test
    void testCalculateTaxes_zeroMargin_onlyIcmsApplies() {
        // When there is no lucro the taxable margem is 0;
        // PIS / COFINS / CSLL / IRPJ must be zero, ICMS is still due on venda preco.
        BigDecimal precoVenda  = new BigDecimal("120000");
        BigDecimal taxableMargem = BigDecimal.ZERO;

        ImpostoBreakdown result = calculator.calcularImpostos(precoVenda, taxableMargem);

        assertThat(result.icms()).isEqualByComparingTo("720.00");
        assertThat(result.pis()).isEqualByComparingTo("0.00");
        assertThat(result.cofins()).isEqualByComparingTo("0.00");
        assertThat(result.csll()).isEqualByComparingTo("0.00");
        assertThat(result.irpj()).isEqualByComparingTo("0.00");
        assertThat(result.totalImpostos()).isEqualByComparingTo("720.00");
    }

    @Test
    void testCalculateTaxes_nullInputs_returnsAllZeros() {
        ImpostoBreakdown result = calculator.calcularImpostos(null, null);

        assertThat(result.icms()).isEqualByComparingTo("0.00");
        assertThat(result.pis()).isEqualByComparingTo("0.00");
        assertThat(result.cofins()).isEqualByComparingTo("0.00");
        assertThat(result.csll()).isEqualByComparingTo("0.00");
        assertThat(result.irpj()).isEqualByComparingTo("0.00");
        assertThat(result.totalImpostos()).isEqualByComparingTo("0.00");
    }

    // -------------------------------------------------------------------------
    // buildRelatorio
    // -------------------------------------------------------------------------

    @Test
    void testBuildReport_standardProfitScenario() {
        VeiculoVendido veiculo = new VeiculoVendido(
            UUID.randomUUID(),
            "ABC1234",
            "Toyota",
            "Corolla",
            2022,
            LocalDate.of(2024, 6, 1),
            new BigDecimal("100000"),   // precoCompra
            BigDecimal.ZERO,            // comissaoCompra
            BigDecimal.ZERO,            // custoFrete
            new BigDecimal("120000"),   // precoVenda
            BigDecimal.ZERO,            // totalServicos
            new BigDecimal("0.05")      // taxaComissaoVenda
        );

        RelatorioVeiculosVendidos relatorio = calculator.buildVeiculosVendidos(List.of(veiculo));

        assertThat(relatorio.totalVeiculosVendidos()).isEqualTo(1);
        assertThat(relatorio.valorTotalVendido()).isEqualByComparingTo("120000.00");
        assertThat(relatorio.valorTotalImpostos()).isEqualByComparingTo("2986.00");
        assertThat(relatorio.valorTotalServicos()).isEqualByComparingTo("0.00");
        assertThat(relatorio.valorTotalComissoes()).isEqualByComparingTo("0.00");
        // lucro = baseLucro − impostos − frete − servicos − comissaoIr
        //        = 20,000 − 2,986 = 17,014
        assertThat(relatorio.lucro()).isEqualByComparingTo("17014.00");
    }

    @Test
    void testBuildReport_sellingBelowPurchase_negativeProfitNoMarginTaxes() {
        // When venda preco is below compra preco the taxable margem is clamped
        // to zero, so only ICMS is charged.
        VeiculoVendido veiculo = new VeiculoVendido(
            UUID.randomUUID(),
            "XYZ9999",
            "Honda",
            "Civic",
            2021,
            LocalDate.of(2024, 7, 15),
            new BigDecimal("120000"),   // precoCompra
            BigDecimal.ZERO,            // comissaoCompra
            BigDecimal.ZERO,            // custoFrete
            new BigDecimal("100000"),   // precoVenda
            BigDecimal.ZERO,            // totalServicos
            null                        // taxaComissaoVenda
        );

        RelatorioVeiculosVendidos relatorio = calculator.buildVeiculosVendidos(List.of(veiculo));

        // ICMS = 100,000 × 0.05 × 0.12 = 600.00
        assertThat(relatorio.valorTotalImpostos()).isEqualByComparingTo("600.00");
        // lucro = −20,000 − 600 = −20,600
        assertThat(relatorio.lucro()).isEqualByComparingTo("-20600.00");
    }

    @Test
    void testBuildReport_emptyList_returnsZeroTotals() {
        RelatorioVeiculosVendidos relatorio = calculator.buildVeiculosVendidos(List.of());

        assertThat(relatorio.totalVeiculosVendidos()).isZero();
        assertThat(relatorio.valorTotalVendido()).isEqualByComparingTo("0.00");
        assertThat(relatorio.valorTotalImpostos()).isEqualByComparingTo("0.00");
        assertThat(relatorio.lucro()).isEqualByComparingTo("0.00");
    }
}
