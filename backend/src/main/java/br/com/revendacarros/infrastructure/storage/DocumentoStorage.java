package br.com.revendacarros.infrastructure.storage;

import br.com.revendacarros.domain.model.TipoDocumento;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.core.io.Resource;

public interface DocumentoStorage {

    String store(UUID veiculoId, TipoDocumento tipoDocumento, UUID documentoId, String nomeOriginalArquivo, InputStream inputStream);

    Resource load(String storageKey);

    void delete(String storageKey);
}
