package br.com.carreselling.application.service;

import br.com.carreselling.common.UuidGenerator;
import br.com.carreselling.application.service.model.DocumentSummary;
import br.com.carreselling.domain.exception.NotFoundException;
import br.com.carreselling.domain.model.Document;
import br.com.carreselling.domain.model.DocumentType;
import br.com.carreselling.domain.repository.DocumentRepository;
import br.com.carreselling.domain.repository.VehicleRepository;
import br.com.carreselling.infrastructure.storage.DocumentStorage;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService implements IDocumentService {

    private static final long MAX_FILE_SIZE_BYTES = 20L * 1024 * 1024;

    private final VehicleRepository vehicleRepository;
    private final DocumentRepository documentRepository;
    private final DocumentStorage documentStorage;

    public DocumentService(VehicleRepository vehicleRepository,
                           DocumentRepository documentRepository,
                           DocumentStorage documentStorage) {
        this.vehicleRepository = vehicleRepository;
        this.documentRepository = documentRepository;
        this.documentStorage = documentStorage;
    }

    @Override
    public UUID uploadDocument(int companyId, UUID vehicleId, DocumentType documentType, MultipartFile file) {
        vehicleRepository.findVehicleById(companyId, vehicleId)
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File exceeds maximum size.");
        }
        UUID documentId = UuidGenerator.generate();
        String originalFileName = file.getOriginalFilename() == null ? "document" : file.getOriginalFilename();
        String storageKey;
        try {
            storageKey = documentStorage.store(
                vehicleId,
                documentType,
                documentId,
                originalFileName,
                file.getInputStream()
            );
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store document");
        }
        Document document = new Document(
            documentId,
            companyId,
            vehicleId,
            documentType,
            originalFileName,
            file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
            file.getSize(),
            storageKey,
            Instant.now(),
            "system"
        );
        documentRepository.saveDocument(companyId, document);
        return documentId;
    }

    @Override
    public List<DocumentSummary> listDocuments(int companyId, UUID vehicleId) {
        vehicleRepository.findVehicleById(companyId, vehicleId)
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        return documentRepository.findDocumentByVehicleId(companyId, vehicleId)
            .stream()
            .map(doc -> new DocumentSummary(
                doc.getId(),
                doc.getVehicleId(),
                doc.getDocumentType(),
                doc.getOriginalFileName(),
                doc.getContentType(),
                doc.getSizeBytes(),
                doc.getUploadedAt()
            ))
            .toList();
    }

    @Override
    public Resource downloadDocument(int companyId, UUID vehicleId, UUID documentId) {
        Document document = getDocumentEntity(companyId, vehicleId, documentId);
        return documentStorage.load(document.getStorageKey());
    }

    @Override
    public DocumentSummary getDocument(int companyId, UUID vehicleId, UUID documentId) {
        Document document = getDocumentEntity(companyId, vehicleId, documentId);
        return new DocumentSummary(
            document.getId(),
            document.getVehicleId(),
            document.getDocumentType(),
            document.getOriginalFileName(),
            document.getContentType(),
            document.getSizeBytes(),
            document.getUploadedAt()
        );
    }

    @Override
    public void deleteDocument(int companyId, UUID vehicleId, UUID documentId) {
        Document document = getDocumentEntity(companyId, vehicleId, documentId);
        documentStorage.delete(document.getStorageKey());
        documentRepository.deleteDocument(companyId, documentId);
    }

    private Document getDocumentEntity(int companyId, UUID vehicleId, UUID documentId) {
        Document document = documentRepository.findDocumentById(companyId, documentId)
            .orElseThrow(() -> new NotFoundException("Document not found"));
        if (!document.getVehicleId().equals(vehicleId)) {
            throw new NotFoundException("Document not found for vehicle");
        }
        return document;
    }
}
