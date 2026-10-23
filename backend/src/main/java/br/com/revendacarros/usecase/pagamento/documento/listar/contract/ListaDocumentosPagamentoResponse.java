package br.com.revendacarros.usecase.pagamento.documento.listar.contract;

import java.util.List;

public record ListaDocumentosPagamentoResponse(List<DocumentoPagamentoItem> documentos) {}
