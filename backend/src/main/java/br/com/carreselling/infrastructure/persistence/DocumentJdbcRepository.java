package br.com.carreselling.infrastructure.persistence;

import br.com.carreselling.domain.model.Document;
import br.com.carreselling.domain.model.DocumentType;
import br.com.carreselling.domain.repository.DocumentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DocumentJdbcRepository implements DocumentRepository {

    private final JdbcTemplate jdbcTemplate;

    public DocumentJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Document saveDocument(int companyId, Document document) {
        jdbcTemplate.update("""
                        INSERT INTO documents
                        (id, company_id, vehicle_id, document_type, original_file_name, content_type, size_bytes,
                         storage_key, uploaded_at, uploaded_by)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                document.getId(),
                companyId,
                document.getVehicleId(),
                document.getDocumentType().name(),
                document.getOriginalFileName(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getStorageKey(),
                Timestamp.from(document.getUploadedAt()),
                document.getUploadedBy()
        );
        return document;
    }

    @Override
    public Optional<Document> findDocumentById(int companyId, UUID id) {
        List<Document> result = jdbcTemplate.query("""
                        SELECT * FROM documents WHERE id = ? AND company_id = ?
                        """,
                new DocumentRowMapper(),
                id,
                companyId);
        return result.stream().findFirst();
    }

    @Override
    public List<Document> findDocumentByVehicleId(int companyId, UUID vehicleId) {
        return jdbcTemplate.query("""
                        SELECT * FROM documents WHERE vehicle_id = ? AND company_id = ? ORDER BY uploaded_at DESC
                        """,
                new DocumentRowMapper(),
                vehicleId,
                companyId);
    }

    @Override
    public List<String> findStorageKeyByVehicleId(int companyId, UUID vehicleId) {
        return jdbcTemplate.queryForList("""
                        SELECT storage_key FROM documents WHERE vehicle_id = ? AND company_id = ?""",
                String.class,
                vehicleId,
                companyId);
    }

    @Override
    public void deleteDocument(int companyId, UUID id) {
        jdbcTemplate.update("DELETE FROM documents WHERE id = ? AND company_id = ?", id, companyId);
    }

    @Override
    public void deleteDocumentByVEhicleId(int companyId, UUID vehicleId) {
        jdbcTemplate.update("DELETE FROM documents WHERE vehicle_id = ? AND company_id = ?", vehicleId, companyId);
    }

    private static class DocumentRowMapper implements RowMapper<Document> {

        @Override
        public Document mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = rs.getObject("id", UUID.class);
            int companyId = rs.getInt("company_id");
            UUID vehicleId = rs.getObject("vehicle_id", UUID.class);
            DocumentType documentType = DocumentType.valueOf(rs.getString("document_type"));
            String originalFileName = rs.getString("original_file_name");
            String contentType = rs.getString("content_type");
            long sizeBytes = rs.getLong("size_bytes");
            String storageKey = rs.getString("storage_key");
            Instant uploadedAt = rs.getTimestamp("uploaded_at").toInstant();
            String uploadedBy = rs.getString("uploaded_by");
            return new Document(id, companyId, vehicleId, documentType, originalFileName, contentType, sizeBytes, storageKey, uploadedAt, uploadedBy);
        }
    }
}
