package br.com.revendacarros.application.service.model;

import java.time.Instant;
import java.util.UUID;

public record ResumoDocumentoPagamento(
    UUID id,
    UUID pagamentoId,
    String nomeOriginalArquivo,
    String contentType,
    long tamanhoBytes,
    Instant enviadoEm
) {}
