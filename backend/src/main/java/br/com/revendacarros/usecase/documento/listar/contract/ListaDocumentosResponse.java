package br.com.revendacarros.usecase.documento.listar.contract;

import java.util.List;

public record ListaDocumentosResponse(List<DocumentoItem> documentos) {
}
