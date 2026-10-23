package br.com.revendacarros.application.service.model;

import java.math.BigDecimal;
import java.util.List;

public record PainelFinanceiro(BigDecimal valorSaldoCaixa,
                                 BigDecimal valorEmCaixa,
                                 BigDecimal patrimonio,
                                 int quantidadeVeiculosAtivos,
                                 BigDecimal custoTotalVeiculosAtivos,
                                 BigDecimal lucroVendas,
                                 BigDecimal lucroVendasSemImpostos,
                                 int totalVeiculosVendidos,
                                 BigDecimal lucroCompras,
                                 int totalVeiculosAdquiridos,
                                 BigDecimal totalPagamentos,
                                 List<PontoMensalFinanceiro> evolucaoMensal) {
}
