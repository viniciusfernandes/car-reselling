package br.com.carreselling.infrastructure.storage;

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
    public String store(UUID vehicleId, UUID documentId, String originalFileName, InputStream inputStream) {
        String sanitized = originalFileName.replaceAll("[\\\\/]", "_");
        Path relativePath = Path.of(vehicleId.toString(), documentId.toString(), sanitized);
        Path targetPath = basePath.resolve(relativePath);
        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return relativePath.toString();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store document");
        }
    }

    @Override
    public Resource load(String storageKey) {
        Path targetPath = basePath.resolve(storageKey);
        return new FileSystemResource(targetPath);
    }

    @Override
    public void delete(String storageKey) {
        Path targetPath = basePath.resolve(storageKey);
        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to delete document");
        }
    }
}
