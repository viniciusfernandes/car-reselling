package br.com.revendacarros.application.service;

import br.com.revendacarros.common.UuidGenerator;
import br.com.revendacarros.application.service.model.ResumoDocumento;
import br.com.revendacarros.domain.exception.NotFoundException;
import br.com.revendacarros.domain.model.Documento;
import br.com.revendacarros.domain.model.TipoDocumento;
import br.com.revendacarros.domain.repository.DocumentoRepository;
import br.com.revendacarros.domain.repository.VeiculoRepository;
import br.com.revendacarros.infrastructure.storage.DocumentoStorage;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentoService implements IDocumentoService {

    private static final long TAMANHO_MAXIMO_ARQUIVO_BYTES = 20L * 1024 * 1024;

    private final VeiculoRepository veiculoRepository;
    private final DocumentoRepository documentoRepository;
    private final DocumentoStorage documentoStorage;

    public DocumentoService(VeiculoRepository veiculoRepository,
                           DocumentoRepository documentoRepository,
                           DocumentoStorage documentoStorage) {
        this.veiculoRepository = veiculoRepository;
        this.documentoRepository = documentoRepository;
        this.documentoStorage = documentoStorage;
    }

    @Override
    public UUID uploadDocumento(int empresaId, UUID veiculoId, TipoDocumento tipoDocumento, MultipartFile file) {
        veiculoRepository.findVeiculoById(empresaId, veiculoId)
            .orElseThrow(() -> new NotFoundException("Veículo não encontrado"));
        if (file.getSize() > TAMANHO_MAXIMO_ARQUIVO_BYTES) {
            throw new IllegalArgumentException("O arquivo excede o tamanho máximo.");
        }
        UUID documentoId = UuidGenerator.generate();
        String nomeOriginalArquivo = file.getOriginalFilename() == null ? "documento" : file.getOriginalFilename();
        String storageKey;
        try {
            storageKey = documentoStorage.store(
                veiculoId,
                tipoDocumento,
                documentoId,
                nomeOriginalArquivo,
                file.getInputStream()
            );
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao armazenar documento");
        }
        Documento documento = new Documento(
            documentoId,
            empresaId,
            veiculoId,
            tipoDocumento,
            nomeOriginalArquivo,
            file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
            file.getSize(),
            storageKey,
            Instant.now(),
            "system"
        );
        documentoRepository.saveDocumento(empresaId, documento);
        return documentoId;
    }

    @Override
    public List<ResumoDocumento> listarDocumentos(int empresaId, UUID veiculoId) {
        veiculoRepository.findVeiculoById(empresaId, veiculoId)
            .orElseThrow(() -> new NotFoundException("Veículo não encontrado"));
        return documentoRepository.findDocumentoByVeiculoId(empresaId, veiculoId)
            .stream()
            .map(doc -> new ResumoDocumento(
                doc.getId(),
                doc.getVeiculoId(),
                doc.getTipoDocumento(),
                doc.getNomeOriginalArquivo(),
                doc.getContentType(),
                doc.getTamanhoBytes(),
                doc.getEnviadoEm()
            ))
            .toList();
    }

    @Override
    public Resource downloadDocumento(int empresaId, UUID veiculoId, UUID documentoId) {
        Documento documento = getDocumentoEntity(empresaId, veiculoId, documentoId);
        return documentoStorage.load(documento.getStorageKey());
    }

    @Override
    public ResumoDocumento getDocumento(int empresaId, UUID veiculoId, UUID documentoId) {
        Documento documento = getDocumentoEntity(empresaId, veiculoId, documentoId);
        return new ResumoDocumento(
            documento.getId(),
            documento.getVeiculoId(),
            documento.getTipoDocumento(),
            documento.getNomeOriginalArquivo(),
            documento.getContentType(),
            documento.getTamanhoBytes(),
            documento.getEnviadoEm()
        );
    }

    @Override
    public void deleteDocumento(int empresaId, UUID veiculoId, UUID documentoId) {
        Documento documento = getDocumentoEntity(empresaId, veiculoId, documentoId);
        documentoStorage.delete(documento.getStorageKey());
        documentoRepository.deleteDocumento(empresaId, documentoId);
    }

    private Documento getDocumentoEntity(int empresaId, UUID veiculoId, UUID documentoId) {
        Documento documento = documentoRepository.findDocumentoById(empresaId, documentoId)
            .orElseThrow(() -> new NotFoundException("Documento não encontrado"));
        if (!documento.getVeiculoId().equals(veiculoId)) {
            throw new NotFoundException("Documento não encontrado para o veículo");
        }
        return documento;
    }
}
