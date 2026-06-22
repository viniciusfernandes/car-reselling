package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.Document;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository {

    Document saveDocument(int companyId, Document document);

    Optional<Document> findDocumentById(int companyId, UUID id);

    List<Document> findDocumentByVehicleId(int companyId, UUID vehicleId);

    List<String> findStorageKeyByVehicleId(int companyId, UUID vehicleId);

    void deleteDocument(int companyId, UUID id);

    void deleteDocumentByVEhicleId(int companyId, UUID vehicleId);
}
