package br.com.carreselling.application.service.model;

import br.com.carreselling.domain.model.DocumentType;
import java.time.Instant;
import java.util.UUID;

public record DocumentSummary(UUID id,
                              UUID vehicleId,
                              DocumentType documentType,
                              String originalFileName,
                              String contentType,
                              long sizeBytes,
                              Instant uploadedAt) {
}
