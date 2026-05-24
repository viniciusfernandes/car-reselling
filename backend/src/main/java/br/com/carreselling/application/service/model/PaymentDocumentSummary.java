package br.com.carreselling.application.service.model;

import java.time.Instant;
import java.util.UUID;

public record PaymentDocumentSummary(
    UUID id,
    UUID paymentId,
    String originalFileName,
    String contentType,
    long sizeBytes,
    Instant uploadedAt
) {}
