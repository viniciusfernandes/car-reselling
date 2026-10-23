package br.com.revendacarros.domain.repository;

import br.com.revendacarros.domain.model.DocumentoPagamento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentoPagamentoRepository {

    DocumentoPagamento saveDocumentoPagamento(int empresaId, DocumentoPagamento documento);

    Optional<DocumentoPagamento> findDocumentoPagamentoById(int empresaId, UUID id);

    List<DocumentoPagamento> findDocumentosPagamentoByPagamentoId(int empresaId, UUID pagamentoId);

    void deleteDocumentoPagamento(int empresaId, UUID id);

    void deleteDocumentosPagamentoByPagamentoId(int empresaId, UUID pagamentoId);
}
