package br.com.revendacarros.domain.repository;

import br.com.revendacarros.application.service.model.CustoTotalVeiculos;
import br.com.revendacarros.application.service.model.FiltroVeiculosDistribuidos;
import br.com.revendacarros.application.service.model.VeiculoDistribuido;
import br.com.revendacarros.application.service.model.VeiculoVendido;
import br.com.revendacarros.domain.model.Veiculo;
import br.com.revendacarros.domain.model.StatusVeiculo;

import java.math.BigDecimal;
import java.util.*;

public interface VeiculoRepository {

    Veiculo saveVeiculo(int empresaId, Veiculo veiculo);

    Optional<Veiculo> findVeiculoById(int empresaId, UUID id);

    Optional<Veiculo> findVeiculoByPlaca(int empresaId, String placa);

    Optional<Veiculo> findVeiculoByRenavam(int empresaId, String renavam);

    Optional<Veiculo> findVeiculoByChassi(int empresaId, String chassi);

    List<Veiculo> findVeiculoByFilter(int empresaId, StatusVeiculo status, String query, Boolean emServico, int page, int size);

    long countVeiculoByFilter(int empresaId, StatusVeiculo status, String query, Boolean emServico);

    Veiculo updateVeiculo(int empresaId, Veiculo veiculo);

    void deleteVeiculo(int empresaId, UUID id);

    BigDecimal findVeiculoTotalServicosByVeiculoId(int empresaId, UUID veiculoId);

    int countVeiculoDocumentosByVeiculoId(int empresaId, UUID veiculoId);

    List<VeiculoDistribuido> relatorioVeiculosDistribuidos(int empresaId, FiltroVeiculosDistribuidos filter);

    List<VeiculoVendido> findTotalServicosFromVeiculosVendidos(int empresaId, FiltroVeiculosDistribuidos filter);

    CustoTotalVeiculos findVeiculoCustoTotal(int empresaId);
}
