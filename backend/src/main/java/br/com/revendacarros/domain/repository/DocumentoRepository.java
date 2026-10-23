package br.com.revendacarros.domain.repository;

import br.com.revendacarros.domain.model.Documento;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentoRepository {

    Documento saveDocumento(int empresaId, Documento documento);

    Optional<Documento> findDocumentoById(int empresaId, UUID id);

    List<Documento> findDocumentoByVeiculoId(int empresaId, UUID veiculoId);

    List<String> findStorageKeyByVeiculoId(int empresaId, UUID veiculoId);

    void deleteDocumento(int empresaId, UUID id);

    void deleteDocumentoByVeiculoId(int empresaId, UUID veiculoId);
}
