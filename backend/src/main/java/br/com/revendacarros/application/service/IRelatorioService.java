package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.model.FiltroVeiculosDistribuidos;
import br.com.revendacarros.application.service.model.RelatorioVeiculosDistribuidos;
import br.com.revendacarros.application.service.model.RelatorioVeiculosVendidos;

public interface IRelatorioService {

    RelatorioVeiculosDistribuidos relatorioVeiculosDistribuidos(int empresaId, FiltroVeiculosDistribuidos filter);

    RelatorioVeiculosVendidos relatorioVeiculosVendidos(int empresaId, FiltroVeiculosDistribuidos filter);
}
