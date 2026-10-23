package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.model.*;
import br.com.revendacarros.domain.repository.SaldoCaixaRepository;
import br.com.revendacarros.domain.repository.PagamentoRepository;
import br.com.revendacarros.domain.repository.VeiculoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PainelService implements IPainelService {

    private final VeiculoRepository veiculoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final CalculadoraVendasVeiculo vendasCalculator;
    private final SaldoCaixaRepository saldoCaixaRepository;

    public PainelService(VeiculoRepository veiculoRepository,
                            PagamentoRepository pagamentoRepository,
                            CalculadoraVendasVeiculo vendasCalculator,
                            SaldoCaixaRepository saldoCaixaRepository) {
        this.veiculoRepository = veiculoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.vendasCalculator = vendasCalculator;
        this.saldoCaixaRepository = saldoCaixaRepository;
    }

    @Override
    public PainelFinanceiro getPainelFinanceiro(int empresaId, LocalDate dataInicio, LocalDate dataFim) {
        LocalDate today = LocalDate.now();
        LocalDate start = dataInicio != null ? dataInicio : today.minusMonths(12).withDayOfMonth(1);
        LocalDate end = dataFim != null ? dataFim : today;

        BigDecimal saldoCaixa = saldoCaixaRepository.findSaldoCaixa(empresaId).valor;

        List<VeiculoVendido> veiculosVendidos = veiculoRepository.findTotalServicosFromVeiculosVendidos(
                empresaId, new FiltroVeiculosDistribuidos(start, end, null, null, null));
        RelatorioVeiculosVendidos vendidoRelatorio = vendasCalculator.buildVeiculosVendidos(veiculosVendidos);

        CustoTotalVeiculos veiculoCusto = veiculoRepository.findVeiculoCustoTotal(empresaId);
        BigDecimal totalPagamentos = pagamentoRepository.findValorTotalPagamentos(empresaId, start, end);
        List<TotalPagamentoMensal> pagamentosMensais = pagamentoRepository.findTotaisPagamentoMensal(empresaId, start, end);

        BigDecimal lucroVendas = vendidoRelatorio.lucro();
        BigDecimal lucroVendasSemImpostos = vendidoRelatorio.lucroAntesImpostos();
        BigDecimal valorEmCaixa = saldoCaixa
                .add(lucroVendasSemImpostos)
                .subtract(veiculoCusto.custoTotal())
                .subtract(totalPagamentos);
        BigDecimal patrimonio = saldoCaixa.add(veiculoCusto.totalPrecoCompra());
        BigDecimal lucroCompras = vendidoRelatorio.valorTotalComissoes()
                .add(veiculoCusto.totalComissaoCompra());
        int totalAdquirido = veiculoCusto.totalVeiculos() + vendidoRelatorio.totalVeiculosVendidos();

        List<PontoMensalFinanceiro> evolucaoMensal = buildEvolucaoMensal(
                vendidoRelatorio.veiculos(), pagamentosMensais);

        return new PainelFinanceiro(
                saldoCaixa,
                valorEmCaixa,
                patrimonio,
                veiculoCusto.totalVeiculos(),
                veiculoCusto.totalPrecoCompra(),
                lucroVendas,
                lucroVendasSemImpostos,
                vendidoRelatorio.totalVeiculosVendidos(),
                lucroCompras,
                totalAdquirido,
                totalPagamentos,
                evolucaoMensal
        );
    }

    private List<PontoMensalFinanceiro> buildEvolucaoMensal(List<ItemVeiculoVendido> vendidoItems,
                                                              List<TotalPagamentoMensal> pagamentosMensais) {
        Map<String, BigDecimal> vendasMap = new LinkedHashMap<>();
        for (ItemVeiculoVendido item : vendidoItems) {
            if (item.dataVenda() == null) {
                continue;
            }
            String key = item.dataVenda().getYear() + "-" + String.format("%02d", item.dataVenda().getMonthValue());
            vendasMap.merge(key, item.lucro(), BigDecimal::add);
        }

        Map<String, BigDecimal> despesasMap = new LinkedHashMap<>();
        for (TotalPagamentoMensal mensal : pagamentosMensais) {
            String key = mensal.ano() + "-" + String.format("%02d", mensal.mes());
            despesasMap.put(key, mensal.total());
        }

        Map<String, BigDecimal[]> combined = new LinkedHashMap<>();
        for (String key : vendasMap.keySet()) {
            combined.computeIfAbsent(key, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO})[0] = vendasMap.get(key);
        }
        for (String key : despesasMap.keySet()) {
            combined.computeIfAbsent(key, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO})[1] = despesasMap.get(key);
        }

        List<String> sortedKeys = new ArrayList<>(combined.keySet());
        sortedKeys.sort(String::compareTo);

        List<PontoMensalFinanceiro> result = new ArrayList<>();
        for (String key : sortedKeys) {
            String[] pecas = key.split("-");
            int ano = Integer.parseInt(pecas[0]);
            int mes = Integer.parseInt(pecas[1]);
            BigDecimal[] values = combined.get(key);
            BigDecimal lucro = values[0];
            BigDecimal despesas = values[1];
            result.add(new PontoMensalFinanceiro(ano, mes, lucro, despesas, lucro.subtract(despesas)));
        }
        return result;
    }
}
