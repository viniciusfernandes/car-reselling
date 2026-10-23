package br.com.revendacarros.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Documento {

    private final UUID id;
    private final int empresaId;
    private final UUID veiculoId;
    private final TipoDocumento tipoDocumento;
    private final String nomeOriginalArquivo;
    private final String contentType;
    private final long tamanhoBytes;
    private final String storageKey;
    private final Instant enviadoEm;
    private final String enviadoPor;

    public Documento(UUID id,
                    int empresaId,
                    UUID veiculoId,
                    TipoDocumento tipoDocumento,
                    String nomeOriginalArquivo,
                    String contentType,
                    long tamanhoBytes,
                    String storageKey,
                    Instant enviadoEm,
                    String enviadoPor) {
        this.id = id;
        this.empresaId = empresaId;
        this.veiculoId = veiculoId;
        this.tipoDocumento = tipoDocumento;
        this.nomeOriginalArquivo = nomeOriginalArquivo;
        this.contentType = contentType;
        this.tamanhoBytes = tamanhoBytes;
        this.storageKey = storageKey;
        this.enviadoEm = enviadoEm;
        this.enviadoPor = enviadoPor;
    }

    public UUID getId() {
        return id;
    }

    public int getEmpresaId() {
        return empresaId;
    }

    public UUID getVeiculoId() {
        return veiculoId;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNomeOriginalArquivo() {
        return nomeOriginalArquivo;
    }

    public String getContentType() {
        return contentType;
    }

    public long getTamanhoBytes() {
        return tamanhoBytes;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public Instant getEnviadoEm() {
        return enviadoEm;
    }

    public String getEnviadoPor() {
        return enviadoPor;
    }
}
