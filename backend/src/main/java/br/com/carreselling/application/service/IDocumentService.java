package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.DocumentSummary;
import br.com.carreselling.domain.model.DocumentType;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface IDocumentService {

    UUID uploadDocument(UUID vehicleId, DocumentType documentType, MultipartFile file);

    List<DocumentSummary> listDocuments(UUID vehicleId);

    Resource downloadDocument(UUID vehicleId, UUID documentId);

    DocumentSummary getDocument(UUID vehicleId, UUID documentId);

    void deleteDocument(UUID vehicleId, UUID documentId);
}
