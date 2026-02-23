package br.com.carreselling.infrastructure.persistence;

import br.com.carreselling.domain.model.PartnerHistory;
import br.com.carreselling.domain.repository.PartnerHistoryRepository;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PartnerHistoryJdbcRepository implements PartnerHistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public PartnerHistoryJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveHistory(PartnerHistory history) {
        jdbcTemplate.update("""
                INSERT INTO partner_history
                (id, partner_id, name, city, phone, email, commission_rate, changed_at, changed_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
            history.getId().toString(),
            history.getPartnerId().toString(),
            history.getName(),
            history.getCity(),
            history.getPhone(),
            history.getEmail(),
            history.getCommissionRate(),
            Timestamp.from(history.getChangedAt()),
            history.getChangedBy()
        );
    }

    @Override
    public List<PartnerHistory> findHistoryByPartnerId(UUID partnerId) {
        return jdbcTemplate.query("""
                SELECT * FROM partner_history
                WHERE partner_id = ?
                ORDER BY changed_at DESC
                """,
            new PartnerHistoryRowMapper(),
            partnerId.toString()
        );
    }

    private static class PartnerHistoryRowMapper implements RowMapper<PartnerHistory> {

        @Override
        public PartnerHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = UUID.fromString(rs.getString("id"));
            UUID partnerId = UUID.fromString(rs.getString("partner_id"));
            String name = rs.getString("name");
            String city = rs.getString("city");
            String phone = rs.getString("phone");
            String email = rs.getString("email");
            BigDecimal commissionRate = rs.getBigDecimal("commission_rate");
            Instant changedAt = rs.getTimestamp("changed_at").toInstant();
            String changedBy = rs.getString("changed_by");
            return new PartnerHistory(id, partnerId, name, city, phone, email,
                commissionRate, changedAt, changedBy);
        }
    }
}
