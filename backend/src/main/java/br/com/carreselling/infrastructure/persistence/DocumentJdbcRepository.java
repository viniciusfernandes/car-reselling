package br.com.carreselling.infrastructure.persistence;

import br.com.carreselling.domain.model.Document;
import br.com.carreselling.domain.model.DocumentType;
import br.com.carreselling.domain.repository.DocumentRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentJdbcRepository implements DocumentRepository {

    private final JdbcTemplate jdbcTemplate;

    public DocumentJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Document saveDocument(Document document) {
        jdbcTemplate.update("""
                INSERT INTO documents
                (id, vehicle_id, document_type, original_file_name, content_type, size_bytes,
                 storage_key, uploaded_at, uploaded_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
            document.getId().toString(),
            document.getVehicleId().toString(),
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
    public Optional<Document> findDocumentById(UUID id) {
        List<Document> result = jdbcTemplate.query("""
                SELECT * FROM documents WHERE id = ?
                """,
            new DocumentRowMapper(),
            id.toString());
        return result.stream().findFirst();
    }

    @Override
    public List<Document> findDocumentByVehicleId(UUID vehicleId) {
        return jdbcTemplate.query("""
                SELECT * FROM documents WHERE vehicle_id = ? ORDER BY uploaded_at DESC
                """,
            new DocumentRowMapper(),
            vehicleId.toString());
    }

    @Override
    public void deleteDocument(UUID id) {
        jdbcTemplate.update("DELETE FROM documents WHERE id = ?", id.toString());
    }

    private static class DocumentRowMapper implements RowMapper<Document> {

        @Override
        public Document mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = UUID.fromString(rs.getString("id"));
            UUID vehicleId = UUID.fromString(rs.getString("vehicle_id"));
            DocumentType documentType = DocumentType.valueOf(rs.getString("document_type"));
            String originalFileName = rs.getString("original_file_name");
            String contentType = rs.getString("content_type");
            long sizeBytes = rs.getLong("size_bytes");
            String storageKey = rs.getString("storage_key");
            Instant uploadedAt = rs.getTimestamp("uploaded_at").toInstant();
            String uploadedBy = rs.getString("uploaded_by");
            return new Document(id, vehicleId, documentType, originalFileName, contentType, sizeBytes, storageKey, uploadedAt, uploadedBy);
        }
    }
}
