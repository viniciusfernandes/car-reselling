package br.com.revendacarros.infrastructure.storage;

import java.io.InputStream;
import java.util.UUID;
import org.springframework.core.io.Resource;

public interface DocumentoPagamentoStorage {

    String store(UUID pagamentoId, UUID documentoId, String nomeOriginalArquivo, InputStream inputStream);

    Resource load(String storageKey);

    void delete(String storageKey);
}
