package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.model.ItemVeiculoVendido;
import br.com.revendacarros.application.service.model.VeiculoVendido;
import br.com.revendacarros.application.service.model.RelatorioVeiculosVendidos;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CalculadoraVendasVeiculo {

    public RelatorioVeiculosVendidos buildVeiculosVendidos(List<VeiculoVendido> veiculos) {
        List<ItemVeiculoVendido> items = new ArrayList<>();
        BigDecimal totalVendido = BigDecimal.ZERO;
        BigDecimal totalImpostos = BigDecimal.ZERO;
        BigDecimal totalServicos = BigDecimal.ZERO;
        BigDecimal totalComissao = BigDecimal.ZERO;
        BigDecimal totalComissaoIr = BigDecimal.ZERO;
        BigDecimal totalLucro = BigDecimal.ZERO;

        for (VeiculoVendido veiculo : veiculos) {
            BigDecimal precoVenda = veiculo.precoVenda();
            BigDecimal totalServicosVeiculo = veiculo.totalServicos();
            BigDecimal comissaoCompra = veiculo.comissaoCompra() == null
                ? BigDecimal.ZERO
                : veiculo.comissaoCompra();
            BigDecimal baseLucro = precoVenda.subtract(veiculo.precoCompra());
            BigDecimal taxableMargem = baseLucro.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : baseLucro;

            ImpostoBreakdown impostos = calcularImpostos(precoVenda, taxableMargem);
            BigDecimal comissaoIr = comissaoCompra
                    .multiply(TaxaImposto.IR_COMISSAO.value())
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal lucroNoImpostos = baseLucro
                .subtract(veiculo.custoFrete())
                .subtract(totalServicosVeiculo)
                .subtract(comissaoIr);

            BigDecimal lucro = lucroNoImpostos.subtract(impostos.totalImpostos());
            items.add(new ItemVeiculoVendido(
                veiculo.veiculoId(),
                veiculo.placa(),
                veiculo.marca(),
                veiculo.modelo(),
                veiculo.ano(),
                veiculo.dataVenda(),
                veiculo.precoCompra(),
                precoVenda,
                impostos.totalImpostos(),
                totalServicosVeiculo,
                comissaoCompra,
                lucro,
                lucroNoImpostos,
                veiculo.taxaComissaoVenda()
            ));
            totalVendido = totalVendido.add(precoVenda);
            totalImpostos = totalImpostos.add(impostos.totalImpostos());
            totalServicos = totalServicos.add(totalServicosVeiculo);
            totalComissao = totalComissao.add(comissaoCompra);
            totalComissaoIr = totalComissaoIr.add(comissaoIr);
            totalLucro = totalLucro.add(lucro);
        }

        return new RelatorioVeiculosVendidos(
            items,
            items.size(),
            totalVendido,
            totalImpostos,
            totalServicos,
            totalComissao,
            totalLucro,
            totalLucro.add(totalImpostos)
        );
    }

    public ImpostoBreakdown calcularImpostos(BigDecimal precoVenda, BigDecimal taxableMargem) {
        if (precoVenda == null || taxableMargem == null) {
            return new ImpostoBreakdown(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
            );
        }
        BigDecimal icms = precoVenda
                .multiply(TaxaImposto.ICMS_BASE.value())
                .multiply(TaxaImposto.ICMS.value());
        BigDecimal pis = taxableMargem.multiply(TaxaImposto.PIS.value());
        BigDecimal cofins = taxableMargem.multiply(TaxaImposto.COFINS.value());
        BigDecimal csll = taxableMargem.multiply(TaxaImposto.CSLL.value());
        BigDecimal irpj = taxableMargem.multiply(TaxaImposto.IRPJ.value());
        BigDecimal total = icms
            .add(pis)
            .add(cofins)
            .add(csll)
            .add(irpj)
            .setScale(2, RoundingMode.HALF_UP);
        return new ImpostoBreakdown(
            icms.setScale(2, RoundingMode.HALF_UP),
            pis.setScale(2, RoundingMode.HALF_UP),
            cofins.setScale(2, RoundingMode.HALF_UP),
            csll.setScale(2, RoundingMode.HALF_UP),
            irpj.setScale(2, RoundingMode.HALF_UP),
            total
        );
    }

    public record ImpostoBreakdown(BigDecimal icms,
                               BigDecimal pis,
                               BigDecimal cofins,
                               BigDecimal csll,
                               BigDecimal irpj,
                               BigDecimal totalImpostos) {
    }

}
