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
    public PaymentDocument savePaymentDocument(int companyId, PaymentDocument document) {
        jdbcTemplate.update("""
                        INSERT INTO payment_documents
                        (id, company_id, payment_id, original_file_name, content_type, size_bytes,
                         storage_key, uploaded_at, uploaded_by)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                document.id.toString(),
                companyId,
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
    public Optional<PaymentDocument> findPaymentDocumentById(int companyId, UUID id) {
        List<PaymentDocument> result = jdbcTemplate.query("""
                        SELECT * FROM payment_documents WHERE id = ? AND company_id = ?
                        """,
                new PaymentDocumentRowMapper(),
                id.toString(),
                companyId);
        return result.stream().findFirst();
    }

    @Override
    public List<PaymentDocument> findPaymentDocumentsByPaymentId(int companyId, UUID paymentId) {
        return jdbcTemplate.query("""
                        SELECT * FROM payment_documents WHERE payment_id = ? AND company_id = ? ORDER BY uploaded_at DESC
                        """,
                new PaymentDocumentRowMapper(),
                paymentId.toString(),
                companyId);
    }

    @Override
    public void deletePaymentDocument(int companyId, UUID id) {
        jdbcTemplate.update("DELETE FROM payment_documents WHERE id = ? AND company_id = ?", id.toString(), companyId);
    }

    @Override
    public void deletePaymentDocumentsByPaymentId(int companyId, UUID paymentId) {
        jdbcTemplate.update("DELETE FROM payment_documents WHERE payment_id = ? AND company_id = ?", paymentId.toString(), companyId);
    }

    private static class PaymentDocumentRowMapper implements RowMapper<PaymentDocument> {

        @Override
        public PaymentDocument mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = UUID.fromString(rs.getString("id"));
            int companyId = rs.getInt("company_id");
            UUID paymentId = UUID.fromString(rs.getString("payment_id"));
            String originalFileName = rs.getString("original_file_name");
            String contentType = rs.getString("content_type");
            long sizeBytes = rs.getLong("size_bytes");
            String storageKey = rs.getString("storage_key");
            Instant uploadedAt = rs.getTimestamp("uploaded_at").toInstant();
            String uploadedBy = rs.getString("uploaded_by");
            return new PaymentDocument(id, companyId, paymentId, originalFileName, contentType,
                    sizeBytes, storageKey, uploadedAt, uploadedBy);
        }
    }
}
