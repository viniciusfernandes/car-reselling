package br.com.carreselling.usecase.payment.document.list.contract;

import java.time.Instant;
import java.util.UUID;

public record PaymentDocumentItem(
    UUID id,
    UUID paymentId,
    String originalFileName,
    String contentType,
    long sizeBytes,
    Instant uploadedAt
) {}
