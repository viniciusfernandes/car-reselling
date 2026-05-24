package br.com.carreselling.infrastructure.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class LocalPaymentDocumentStorage implements PaymentDocumentStorage {

    private static final String PAYMENTS_DIR = "payments";

    private final Path basePath;
    private final Logger log = LoggerFactory.getLogger(LocalPaymentDocumentStorage.class);

    public LocalPaymentDocumentStorage(@Value("${storage.base-path}") String basePath) {
        this.basePath = Path.of(basePath);
    }

    @Override
    public String store(UUID paymentId, UUID documentId, String originalFileName, InputStream inputStream) {
        String sanitized = originalFileName.replaceAll("[\\\\/]", "_");
        String storedFileName = documentId + "_" + sanitized;
        Path relativePath = Path.of(PAYMENTS_DIR, paymentId.toString(), storedFileName);
        Path targetPath = basePath.resolve(relativePath);
        log.info("Uploading payment document {}", targetPath);
        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return relativePath.toString();
        } catch (IOException ex) {
            throw new DocumentStorageException(
                    "Failed to store payment document: paymentId=%s, documentId=%s, originalFileName='%s', targetPath=%s"
                            .formatted(paymentId, documentId, originalFileName, targetPath),
                    ex);
        }
    }

    @Override
    public Resource load(String storageKey) {
        Path targetPath = basePath.resolve(storageKey);
        log.info("Downloading payment document {}", targetPath);
        if (!Files.exists(targetPath)) {
            throw new DocumentStorageException(
                    "Payment document not found: storageKey='%s', resolvedPath=%s".formatted(storageKey, targetPath));
        }
        return new FileSystemResource(targetPath);
    }

    @Override
    public void delete(String storageKey) {
        Path targetPath = basePath.resolve(storageKey);
        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException ex) {
            throw new DocumentStorageException(
                    "Failed to delete payment document: storageKey='%s', resolvedPath=%s".formatted(storageKey, targetPath),
                    ex);
        }
    }
}
