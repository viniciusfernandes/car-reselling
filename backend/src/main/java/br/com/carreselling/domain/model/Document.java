package br.com.carreselling.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Document {

    private final UUID id;
    private final int companyId;
    private final UUID vehicleId;
    private final DocumentType documentType;
    private final String originalFileName;
    private final String contentType;
    private final long sizeBytes;
    private final String storageKey;
    private final Instant uploadedAt;
    private final String uploadedBy;

    public Document(UUID id,
                    int companyId,
                    UUID vehicleId,
                    DocumentType documentType,
                    String originalFileName,
                    String contentType,
                    long sizeBytes,
                    String storageKey,
                    Instant uploadedAt,
                    String uploadedBy) {
        this.id = id;
        this.companyId = companyId;
        this.vehicleId = vehicleId;
        this.documentType = documentType;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.storageKey = storageKey;
        this.uploadedAt = uploadedAt;
        this.uploadedBy = uploadedBy;
    }

    public UUID getId() {
        return id;
    }

    public int getCompanyId() {
        return companyId;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }
}
