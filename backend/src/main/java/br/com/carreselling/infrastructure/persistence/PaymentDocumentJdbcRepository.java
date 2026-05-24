package br.com.carreselling.infrastructure.persistence;

import br.com.carreselling.domain.model.PaymentDocument;
import br.com.carreselling.domain.repository.PaymentDocumentRepository;
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
public class PaymentDocumentJdbcRepository implements PaymentDocumentRepository {

    private final JdbcTemplate jdbcTemplate;

    public PaymentDocumentJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PaymentDocument savePaymentDocument(PaymentDocument document) {
        jdbcTemplate.update("""
                        INSERT INTO payment_documents
                        (id, payment_id, original_file_name, content_type, size_bytes,
                         storage_key, uploaded_at, uploaded_by)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                document.id.toString(),
                document.paymentId.toString(),
                document.originalFileName,
                document.contentType,
                document.sizeBytes,
                document.storageKey,
                Timestamp.from(document.uploadedAt),
                document.uploadedBy
        );
        return document;
    }

    @Override
    public Optional<PaymentDocument> findPaymentDocumentById(UUID id) {
        List<PaymentDocument> result = jdbcTemplate.query("""
                        SELECT * FROM payment_documents WHERE id = ?
                        """,
                new PaymentDocumentRowMapper(),
                id.toString());
        return result.stream().findFirst();
    }

    @Override
    public List<PaymentDocument> findPaymentDocumentsByPaymentId(UUID paymentId) {
        return jdbcTemplate.query("""
                        SELECT * FROM payment_documents WHERE payment_id = ? ORDER BY uploaded_at DESC
                        """,
                new PaymentDocumentRowMapper(),
                paymentId.toString());
    }

    @Override
    public void deletePaymentDocument(UUID id) {
        jdbcTemplate.update("DELETE FROM payment_documents WHERE id = ?", id.toString());
    }

    @Override
    public void deletePaymentDocumentsByPaymentId(UUID paymentId) {
        jdbcTemplate.update("DELETE FROM payment_documents WHERE payment_id = ?", paymentId.toString());
    }

    private static class PaymentDocumentRowMapper implements RowMapper<PaymentDocument> {

        @Override
        public PaymentDocument mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = UUID.fromString(rs.getString("id"));
            UUID paymentId = UUID.fromString(rs.getString("payment_id"));
            String originalFileName = rs.getString("original_file_name");
            String contentType = rs.getString("content_type");
            long sizeBytes = rs.getLong("size_bytes");
            String storageKey = rs.getString("storage_key");
            Instant uploadedAt = rs.getTimestamp("uploaded_at").toInstant();
            String uploadedBy = rs.getString("uploaded_by");
            return new PaymentDocument(id, paymentId, originalFileName, contentType,
                    sizeBytes, storageKey, uploadedAt, uploadedBy);
        }
    }
}
