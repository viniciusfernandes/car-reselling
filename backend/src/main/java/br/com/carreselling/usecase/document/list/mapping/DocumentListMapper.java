package br.com.carreselling.usecase.document.list.mapping;

import br.com.carreselling.application.service.model.DocumentSummary;
import br.com.carreselling.usecase.document.list.contract.DocumentItem;

public class DocumentListMapper {

    private DocumentListMapper() {
    }

    public static DocumentItem toItem(DocumentSummary summary) {
        return new DocumentItem(
            summary.id(),
            summary.vehicleId(),
            summary.documentType(),
            summary.originalFileName(),
            summary.contentType(),
            summary.sizeBytes(),
            summary.uploadedAt()
        );
    }
}
