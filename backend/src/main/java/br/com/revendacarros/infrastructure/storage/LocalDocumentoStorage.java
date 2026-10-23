package br.com.revendacarros.infrastructure.storage;

import br.com.revendacarros.domain.model.TipoDocumento;

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
public class LocalDocumentoStorage implements DocumentoStorage {

    private final Path basePath;
    private final Logger log = LoggerFactory.getLogger(LocalDocumentoStorage.class);

    public LocalDocumentoStorage(@Value("${storage.base-path}") String basePath) {
        this.basePath = Path.of(basePath);
    }

    @Override
    public String store(UUID veiculoId, TipoDocumento tipoDocumento, UUID documentoId, String nomeOriginalArquivo, InputStream inputStream) {
        String sanitized = nomeOriginalArquivo.replaceAll("[\\\\/]", "_");
        String nomeArquivoArmazenado = documentoId + "_" + sanitized;
        Path relativePath = Path.of(veiculoId.toString(), tipoDocumento.name(), nomeArquivoArmazenado);
        Path targetPath = basePath.resolve(relativePath);
        log.info("Arquivo do veículo enviado {}", targetPath);
        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return relativePath.toString();
        } catch (IOException ex) {
            throw new DocumentoStorageException(
                    "Falha ao armazenar documento: veiculoId=%s, tipoDocumento=%s, documentoId=%s, nomeOriginalArquivo='%s', targetPath=%s"
                            .formatted(veiculoId, tipoDocumento, documentoId, nomeOriginalArquivo, targetPath),
                    ex);
        }
    }

    @Override
    public Resource load(String storageKey) {
        Path targetPath = basePath.resolve(storageKey);
        log.info("Arquivo do veículo baixado {}", targetPath);
        if (!Files.exists(targetPath)) {
            throw new DocumentoStorageException(
                    "Documento não encontrado: storageKey='%s', resolvedPath=%s".formatted(storageKey, targetPath));
        }
        return new FileSystemResource(targetPath);
    }

    @Override
    public void delete(String storageKey) {
        Path targetPath = basePath.resolve(storageKey);
        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException ex) {
            throw new DocumentoStorageException(
                    "Falha ao excluir documento: storageKey='%s', resolvedPath=%s".formatted(storageKey, targetPath),
                    ex);
        }
    }
}
