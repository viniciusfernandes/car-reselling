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
    public Partner savePartner(Partner partner) {
        jdbcTemplate.update("""
                INSERT INTO partners
                (id, name, city, phone, email, commission_rate, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
            partner.getId().toString(),
            partner.getName(),
            partner.getCity(),
            partner.getPhone(),
            partner.getEmail(),
            partner.getCommissionRate(),
            Timestamp.from(partner.getCreatedAt()),
            partner.getUpdatedAt() == null ? null : Timestamp.from(partner.getUpdatedAt())
        );
        return partner;
    }

    @Override
    public Partner updatePartner(Partner partner) {
        jdbcTemplate.update("""
                UPDATE partners
                SET name = ?, city = ?, phone = ?, email = ?, commission_rate = ?, updated_at = ?
                WHERE id = ?
                """,
            partner.getName(),
            partner.getCity(),
            partner.getPhone(),
            partner.getEmail(),
            partner.getCommissionRate(),
            Timestamp.from(partner.getUpdatedAt()),
            partner.getId().toString()
        );
        return partner;
    }

    @Override
    public List<Partner> findPartner() {
        return jdbcTemplate.query("""
                SELECT * FROM partners ORDER BY name ASC
                """,
            new PartnerRowMapper());
    }

    @Override
    public Optional<Partner> findPartnerById(UUID id) {
        List<Partner> result = jdbcTemplate.query("""
                SELECT * FROM partners WHERE id = ?
                """,
            new PartnerRowMapper(),
            id.toString());
        return result.stream().findFirst();
    }

    @Override
    public Optional<Partner> findPartnerByName(String name) {
        List<Partner> result = jdbcTemplate.query("""
                SELECT * FROM partners WHERE name = ?
                """,
            new PartnerRowMapper(),
            name);
        return result.stream().findFirst();
    }

    private static class PartnerRowMapper implements RowMapper<Partner> {

        @Override
        public Partner mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = UUID.fromString(rs.getString("id"));
            String name = rs.getString("name");
            String city = rs.getString("city");
            String phone = rs.getString("phone");
            String email = rs.getString("email");
            BigDecimal commissionRate = rs.getBigDecimal("commission_rate");
            Instant createdAt = rs.getTimestamp("created_at").toInstant();
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            return new Partner(
                id,
                name,
                city,
                phone,
                email,
                commissionRate,
                createdAt,
                updatedAt == null ? null : updatedAt.toInstant()
            );
        }
    }
}
