package br.com.carreselling.infrastructure.storage;

import java.io.InputStream;
import java.util.UUID;
import org.springframework.core.io.Resource;

public interface PaymentDocumentStorage {

    String store(UUID paymentId, UUID documentId, String originalFileName, InputStream inputStream);

    Resource load(String storageKey);

    void delete(String storageKey);
}
