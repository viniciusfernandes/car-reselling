package br.com.revendacarros.infrastructure.storage;

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
public class LocalDocumentoPagamentoStorage implements DocumentoPagamentoStorage {

    private static final String PAGAMENTOS_DIR = "pagamentos";

    private final Path basePath;
    private final Logger log = LoggerFactory.getLogger(LocalDocumentoPagamentoStorage.class);

    public LocalDocumentoPagamentoStorage(@Value("${storage.base-path}") String basePath) {
        this.basePath = Path.of(basePath);
    }

    @Override
    public String store(UUID pagamentoId, UUID documentoId, String nomeOriginalArquivo, InputStream inputStream) {
        String sanitized = nomeOriginalArquivo.replaceAll("[\\\\/]", "_");
        String nomeArquivoArmazenado = documentoId + "_" + sanitized;
        Path relativePath = Path.of(PAGAMENTOS_DIR, pagamentoId.toString(), nomeArquivoArmazenado);
        Path targetPath = basePath.resolve(relativePath);
        log.info("Enviando documento de pagamento {}", targetPath);
        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return relativePath.toString();
        } catch (IOException ex) {
            throw new DocumentoStorageException(
                    "Falha ao armazenar documento de pagamento: pagamentoId=%s, documentoId=%s, nomeOriginalArquivo='%s', targetPath=%s"
                            .formatted(pagamentoId, documentoId, nomeOriginalArquivo, targetPath),
                    ex);
        }
    }

    @Override
    public Resource load(String storageKey) {
        Path targetPath = basePath.resolve(storageKey);
        log.info("Baixando documento de pagamento {}", targetPath);
        if (!Files.exists(targetPath)) {
            throw new DocumentoStorageException(
                    "Documento de pagamento não encontrado: storageKey='%s', resolvedPath=%s".formatted(storageKey, targetPath));
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
                    "Falha ao excluir documento de pagamento: storageKey='%s', resolvedPath=%s".formatted(storageKey, targetPath),
                    ex);
        }
    }
}
