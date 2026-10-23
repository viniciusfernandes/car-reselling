package br.com.revendacarros.application.service.model;

import br.com.revendacarros.domain.model.TipoDocumento;
import java.time.Instant;
import java.util.UUID;

public record ResumoDocumento(UUID id,
                              UUID veiculoId,
                              TipoDocumento tipoDocumento,
                              String nomeOriginalArquivo,
                              String contentType,
                              long tamanhoBytes,
                              Instant enviadoEm) {
}
