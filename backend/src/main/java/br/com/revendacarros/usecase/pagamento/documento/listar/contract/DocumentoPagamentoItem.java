package br.com.revendacarros.usecase.pagamento.documento.listar.contract;

import java.time.Instant;
import java.util.UUID;

public record DocumentoPagamentoItem(
    UUID id,
    UUID pagamentoId,
    String nomeOriginalArquivo,
    String contentType,
    long tamanhoBytes,
    Instant enviadoEm
) {}
