package br.com.carreselling.infrastructure.storage;

import br.com.carreselling.domain.model.DocumentType;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.core.io.Resource;

public interface DocumentStorage {

    String store(UUID vehicleId, DocumentType documentType, UUID documentId, String originalFileName, InputStream inputStream);

    Resource load(String storageKey);

    void delete(String storageKey);
}
