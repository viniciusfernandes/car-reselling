package br.com.carreselling.infrastructure.storage;

import br.com.carreselling.domain.model.DocumentType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class LocalDocumentStorage implements DocumentStorage {

    private final Path basePath;

    public LocalDocumentStorage(@Value("${storage.base-path}") String basePath) {
        this.basePath = Path.of(basePath);
    }

    @Override
    public String store(UUID vehicleId, DocumentType documentType, UUID documentId, String originalFileName, InputStream inputStream) {
        String sanitized = originalFileName.replaceAll("[\\\\/]", "_");
        String storedFileName = documentId + "_" + sanitized;
        Path relativePath = Path.of(vehicleId.toString(), documentType.name(), storedFileName);
        Path targetPath = basePath.resolve(relativePath);
        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return relativePath.toString();
        } catch (IOException ex) {
            throw new DocumentStorageException(
                    "Failed to store document: vehicleId=%s, documentType=%s, documentId=%s, originalFileName='%s', targetPath=%s"
                            .formatted(vehicleId, documentType, documentId, originalFileName, targetPath),
                    ex);
        }
    }

    @Override
    public Resource load(String storageKey) {
        Path targetPath = basePath.resolve(storageKey);
        if (!Files.exists(targetPath)) {
            throw new DocumentStorageException(
                    "Document not found: storageKey='%s', resolvedPath=%s".formatted(storageKey, targetPath));
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
                    "Failed to delete document: storageKey='%s', resolvedPath=%s".formatted(storageKey, targetPath),
                    ex);
        }
    }
}
