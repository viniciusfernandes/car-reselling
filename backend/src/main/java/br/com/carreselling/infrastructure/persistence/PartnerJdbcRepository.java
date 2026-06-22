package br.com.carreselling.infrastructure.persistence;

import br.com.carreselling.domain.model.Partner;
import br.com.carreselling.domain.repository.PartnerRepository;
import java.math.BigDecimal;
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
public class PartnerJdbcRepository implements PartnerRepository {

    private final JdbcTemplate jdbcTemplate;

    public PartnerJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Partner savePartner(int companyId, Partner partner) {
        jdbcTemplate.update("""
                INSERT INTO partners
                (id, company_id, name, city, phone, email, commission_rate, enabled, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
            partner.getId().toString(),
            companyId,
            partner.getName(),
            partner.getCity(),
            partner.getPhone(),
            partner.getEmail(),
            partner.getCommissionRate(),
            partner.isEnabled(),
            Timestamp.from(partner.getCreatedAt()),
            partner.getUpdatedAt() == null ? null : Timestamp.from(partner.getUpdatedAt())
        );
        return partner;
    }

    @Override
    public Partner updatePartner(int companyId, Partner partner) {
        jdbcTemplate.update("""
                UPDATE partners
                SET name = ?, city = ?, phone = ?, email = ?, commission_rate = ?, enabled = ?, updated_at = ?
                WHERE id = ? AND company_id = ?
                """,
            partner.getName(),
            partner.getCity(),
            partner.getPhone(),
            partner.getEmail(),
            partner.getCommissionRate(),
            partner.isEnabled(),
            Timestamp.from(partner.getUpdatedAt()),
            partner.getId().toString(),
            companyId
        );
        return partner;
    }

    @Override
    public List<Partner> findEnabledPartners(int companyId) {
        return jdbcTemplate.query("""
                SELECT * FROM partners WHERE company_id = ? AND enabled = TRUE ORDER BY name ASC
                """,
            new PartnerRowMapper(),
            companyId);
    }

    @Override
    public void setEnabled(int companyId, UUID id, boolean enabled) {
        jdbcTemplate.update("UPDATE partners SET enabled = ?, updated_at = ? WHERE id = ? AND company_id = ?",
            enabled, Timestamp.from(Instant.now()), id.toString(), companyId);
    }

    @Override
    public Optional<Partner> findPartnerById(int companyId, UUID id) {
        List<Partner> result = jdbcTemplate.query("""
                SELECT * FROM partners WHERE id = ? AND company_id = ?
                """,
            new PartnerRowMapper(),
            id.toString(),
            companyId);
        return result.stream().findFirst();
    }

    @Override
    public Optional<Partner> findPartnerByName(int companyId, String name) {
        List<Partner> result = jdbcTemplate.query("""
                SELECT * FROM partners WHERE name = ? AND company_id = ?
                """,
            new PartnerRowMapper(),
            name,
            companyId);
        return result.stream().findFirst();
    }

    private static class PartnerRowMapper implements RowMapper<Partner> {

        @Override
        public Partner mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = UUID.fromString(rs.getString("id"));
            int companyId = rs.getInt("company_id");
            String name = rs.getString("name");
            String city = rs.getString("city");
            String phone = rs.getString("phone");
            String email = rs.getString("email");
            BigDecimal commissionRate = rs.getBigDecimal("commission_rate");
            Instant createdAt = rs.getTimestamp("created_at").toInstant();
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            boolean enabled = true;
        try {
            enabled = rs.getBoolean("enabled");
        } catch (SQLException ignored) {
            // column may not exist on old DB
        }
        return new Partner(
                id,
                companyId,
                name,
                city,
                phone,
                email,
                commissionRate,
                enabled,
                createdAt,
                updatedAt == null ? null : updatedAt.toInstant()
            );
        }
    }
}
