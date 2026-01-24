package br.com.carreselling.usecase.document.list.contract;

import br.com.carreselling.domain.model.DocumentType;
import java.time.Instant;
import java.util.UUID;

public record DocumentItem(UUID id,
                           UUID vehicleId,
                           DocumentType documentType,
                           String originalFileName,
                           String contentType,
                           long sizeBytes,
                           Instant uploadedAt) {
}
