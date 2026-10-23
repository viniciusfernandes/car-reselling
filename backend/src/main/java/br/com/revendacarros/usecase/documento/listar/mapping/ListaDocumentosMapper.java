package br.com.revendacarros.usecase.documento.listar.mapping;

import br.com.revendacarros.application.service.model.ResumoDocumento;
import br.com.revendacarros.usecase.documento.listar.contract.DocumentoItem;

public class ListaDocumentosMapper {

    private ListaDocumentosMapper() {
    }

    public static DocumentoItem toItem(ResumoDocumento resumo) {
        return new DocumentoItem(
            resumo.id(),
            resumo.veiculoId(),
            resumo.tipoDocumento(),
            resumo.nomeOriginalArquivo(),
            resumo.contentType(),
            resumo.tamanhoBytes(),
            resumo.enviadoEm()
        );
    }
}
