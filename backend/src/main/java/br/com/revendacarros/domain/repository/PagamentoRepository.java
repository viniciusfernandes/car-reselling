package br.com.revendacarros.domain.repository;

import br.com.revendacarros.application.service.model.TotalPagamentoMensal;
import br.com.revendacarros.domain.model.Pagamento;
import br.com.revendacarros.domain.model.TipoPagamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PagamentoRepository {

    Pagamento savePagamento(int empresaId, Pagamento pagamento);

    Optional<Pagamento> findPagamentoById(int empresaId, UUID id);

    List<Pagamento> findPagamentos(int empresaId, TipoPagamento tipoPagamento, Integer anoPagamento, Integer mesPagamento, String placa);

    List<String> findDescricoesDistintas(int empresaId, TipoPagamento tipoPagamento);

    Pagamento updatePagamento(int empresaId, Pagamento pagamento);

    void deletePagamento(int empresaId, UUID id);

    List<Pagamento> findPagamentosByVeiculoId(int empresaId, UUID veiculoId);

    void deletePagamentosByVeiculoId(int empresaId, UUID veiculoId);

    BigDecimal findValorTotalPagamentos(int empresaId, LocalDate dataInicio, LocalDate dataFim);

    List<TotalPagamentoMensal> findTotaisPagamentoMensal(int empresaId, LocalDate dataInicio, LocalDate dataFim);
}
