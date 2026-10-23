package br.com.revendacarros.application.service;

import br.com.revendacarros.application.service.model.ResumoDocumentoPagamento;
import br.com.revendacarros.application.service.model.ResumoPagamento;
import br.com.revendacarros.domain.model.TipoPagamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface IPagamentoService {

    UUID criarPagamento(int empresaId,
                       TipoPagamento tipoPagamento,
                       String descricao,
                       BigDecimal valor,
                       LocalDate dataPagamento,
                       String veiculoPlaca,
                       String observacoes);

    List<ResumoPagamento> listarPagamentos(int empresaId, TipoPagamento tipoPagamento, Integer anoPagamento, Integer mesPagamento, String placa);

    ResumoPagamento getPagamento(int empresaId, UUID id);

    void updatePagamento(int empresaId,
                       UUID id,
                       TipoPagamento tipoPagamento,
                       String descricao,
                       BigDecimal valor,
                       LocalDate dataPagamento,
                       String veiculoPlaca,
                       String observacoes);

    void deletePagamento(int empresaId, UUID id);

    void deletePagamentosByVeiculoId(int empresaId, UUID veiculoId);

    List<String> listarDescricoes(int empresaId, TipoPagamento tipoPagamento);

    UUID uploadDocumentoPagamento(int empresaId, UUID pagamentoId, MultipartFile file);

    List<ResumoDocumentoPagamento> listarDocumentosPagamento(int empresaId, UUID pagamentoId);

    Resource downloadDocumentoPagamento(int empresaId, UUID pagamentoId, UUID documentoId);

    void deleteDocumentoPagamento(int empresaId, UUID pagamentoId, UUID documentoId);
}
