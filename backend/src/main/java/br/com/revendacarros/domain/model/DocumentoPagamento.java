package br.com.revendacarros.domain.model;

import java.time.Instant;
import java.util.UUID;

public class DocumentoPagamento {

    public final UUID id;
    public final int empresaId;
    public final UUID pagamentoId;
    public final String nomeOriginalArquivo;
    public final String contentType;
    public final long tamanhoBytes;
    public final String storageKey;
    public final Instant enviadoEm;
    public final String enviadoPor;

    public DocumentoPagamento(UUID id,
                           int empresaId,
                           UUID pagamentoId,
                           String nomeOriginalArquivo,
                           String contentType,
                           long tamanhoBytes,
                           String storageKey,
                           Instant enviadoEm,
                           String enviadoPor) {
        this.id = id;
        this.empresaId = empresaId;
        this.pagamentoId = pagamentoId;
        this.nomeOriginalArquivo = nomeOriginalArquivo;
        this.contentType = contentType;
        this.tamanhoBytes = tamanhoBytes;
        this.storageKey = storageKey;
        this.enviadoEm = enviadoEm;
        this.enviadoPor = enviadoPor;
    }
}
