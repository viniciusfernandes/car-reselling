package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.model.PainelFinanceiro;

import java.time.LocalDate;

public interface IPainelService {

    PainelFinanceiro getPainelFinanceiro(int empresaId, LocalDate dataInicio, LocalDate dataFim);
}
