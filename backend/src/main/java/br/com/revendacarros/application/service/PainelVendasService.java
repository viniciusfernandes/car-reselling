package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import br.com.revendacarros.domain.repository.VeiculoRepository;
import org.springframework.stereotype.Service;

@Service
public class PainelVendasService implements IRelatorioService {
    private final VeiculoRepository veiculoRepository;
    private final CalculadoraVendasVeiculo vendasCalculator;

    public PainelVendasService(VeiculoRepository veiculoRepository, CalculadoraVendasVeiculo vendasCalculator) {
        this.veiculoRepository = veiculoRepository;
        this.vendasCalculator = vendasCalculator;
    }

    @Override
    public RelatorioVeiculosDistribuidos relatorioVeiculosDistribuidos(int empresaId, FiltroVeiculosDistribuidos filter) {
        List<VeiculoDistribuido> veiculosDistribuidos = veiculoRepository.relatorioVeiculosDistribuidos(empresaId, filter);

        Map<UUID, ParceiroAccumulator> grouped = new LinkedHashMap<>();
        for (VeiculoDistribuido row : veiculosDistribuidos) {
            grouped.computeIfAbsent(row.parceiroId(), key -> new ParceiroAccumulator(row.parceiroId(), row.nomeParceiro()));
            ParceiroAccumulator accumulator = grouped.get(row.parceiroId());
            BigDecimal custoTotal = row.precoCompra()
                    .add(row.custoFrete())
                    .add(row.totalServicos());
            accumulator.veiculos.add(new ItemVeiculoRelatorio(
                    row.veiculoId(),
                    row.placa(),
                    row.marca(),
                    row.modelo(),
                    row.ano(),
                    row.distribuidoEm(),
                    row.precoCompra(),
                    row.comissaoCompra(),
                    custoTotal
            ));
            accumulator.totalValue = accumulator.totalValue.add(row.precoCompra());
        }

        List<GrupoParceiroRelatorio> parceiros = new ArrayList<>();
        int overallCount = 0;
        BigDecimal overallTotal = BigDecimal.ZERO;
        for (ParceiroAccumulator accumulator : grouped.values()) {
            int count = accumulator.veiculos.size();
            parceiros.add(new GrupoParceiroRelatorio(
                    accumulator.parceiroId,
                    accumulator.nomeParceiro,
                    accumulator.veiculos,
                    accumulator.totalValue,
                    count
            ));
            overallCount += count;
            overallTotal = overallTotal.add(accumulator.totalValue);
        }
        return new RelatorioVeiculosDistribuidos(parceiros, overallCount, overallTotal);
    }

    @Override
    public RelatorioVeiculosVendidos relatorioVeiculosVendidos(int empresaId, FiltroVeiculosDistribuidos filter) {
        List<VeiculoVendido> veiculosVendidos = veiculoRepository.findTotalServicosFromVeiculosVendidos(empresaId, filter);
        return vendasCalculator.buildVeiculosVendidos(veiculosVendidos);
    }


    private static class ParceiroAccumulator {
        private final UUID parceiroId;
        private final String nomeParceiro;
        private final List<ItemVeiculoRelatorio> veiculos = new ArrayList<>();
        private BigDecimal totalValue = BigDecimal.ZERO;

        private ParceiroAccumulator(UUID parceiroId, String nomeParceiro) {
            this.parceiroId = parceiroId;
            this.nomeParceiro = nomeParceiro;
        }
    }
}
