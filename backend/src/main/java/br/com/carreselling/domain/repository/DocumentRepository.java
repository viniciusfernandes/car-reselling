package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.Document;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository {

    Document saveDocument(Document document);

    Optional<Document> findDocumentById(UUID id);

    List<Document> findDocumentByVehicleId(UUID vehicleId);

    void deleteDocument(UUID id);
}
