package br.com.revendacarros.usecase.documento.listar.contract;

import br.com.revendacarros.domain.model.TipoDocumento;
import java.time.Instant;
import java.util.UUID;

public record DocumentoItem(UUID id,
                           UUID veiculoId,
                           TipoDocumento tipoDocumento,
                           String nomeOriginalArquivo,
                           String contentType,
                           long tamanhoBytes,
                           Instant enviadoEm) {
}
