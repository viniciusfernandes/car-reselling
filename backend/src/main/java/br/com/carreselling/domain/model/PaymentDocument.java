package br.com.carreselling.domain.model;

import java.time.Instant;
import java.util.UUID;

public class PaymentDocument {

    public final UUID id;
    public final UUID paymentId;
    public final String originalFileName;
    public final String contentType;
    public final long sizeBytes;
    public final String storageKey;
    public final Instant uploadedAt;
    public final String uploadedBy;

    public PaymentDocument(UUID id,
                           UUID paymentId,
                           String originalFileName,
                           String contentType,
                           long sizeBytes,
                           String storageKey,
                           Instant uploadedAt,
                           String uploadedBy) {
        this.id = id;
        this.paymentId = paymentId;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.storageKey = storageKey;
        this.uploadedAt = uploadedAt;
        this.uploadedBy = uploadedBy;
    }
}
