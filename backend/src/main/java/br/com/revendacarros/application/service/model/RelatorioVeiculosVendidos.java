package br.com.revendacarros.application.service.model;

import java.math.BigDecimal;
import java.util.List;

public record RelatorioVeiculosVendidos(List<ItemVeiculoVendido> veiculos,
                                 int totalVeiculosVendidos,
                                 BigDecimal valorTotalVendido,
                                 BigDecimal valorTotalImpostos,
                                 BigDecimal valorTotalServicos,
                                 BigDecimal valorTotalComissoes,
                                 BigDecimal lucro,
                                 BigDecimal lucroAntesImpostos) {
}
