package br.com.carreselling.infrastructure.persistence;

import br.com.carreselling.domain.model.Brand;
import br.com.carreselling.domain.repository.BrandRepository;
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
public class BrandJdbcRepository implements BrandRepository {

    private final JdbcTemplate jdbcTemplate;

    public BrandJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Brand saveBrand(int companyId, Brand brand) {
        jdbcTemplate.update("""
                INSERT INTO brands (id, company_id, name, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """,
            brand.getId().toString(),
            companyId,
            brand.getName(),
            Timestamp.from(brand.getCreatedAt()),
            brand.getUpdatedAt() == null ? null : Timestamp.from(brand.getUpdatedAt())
        );
        return brand;
    }

    @Override
    public List<Brand> findBrands(int companyId) {
        return jdbcTemplate.query("""
                SELECT * FROM brands WHERE company_id = ? ORDER BY name ASC
                """,
            new BrandRowMapper(),
            companyId);
    }

    @Override
    public Optional<Brand> findBrandById(int companyId, UUID id) {
        List<Brand> result = jdbcTemplate.query("""
                SELECT * FROM brands WHERE id = ? AND company_id = ?
                """,
            new BrandRowMapper(),
            id.toString(),
            companyId);
        return result.stream().findFirst();
    }

    @Override
    public Optional<Brand> findBrandByName(int companyId, String name) {
        List<Brand> result = jdbcTemplate.query("""
                SELECT * FROM brands WHERE name = ? AND company_id = ?
                """,
            new BrandRowMapper(),
            name,
            companyId);
        return result.stream().findFirst();
    }

    private static class BrandRowMapper implements RowMapper<Brand> {

        @Override
        public Brand mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = UUID.fromString(rs.getString("id"));
            int companyId = rs.getInt("company_id");
            String name = rs.getString("name");
            Instant createdAt = rs.getTimestamp("created_at").toInstant();
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            return new Brand(
                id,
                companyId,
                name,
                createdAt,
                updatedAt == null ? null : updatedAt.toInstant()
            );
        }
    }
}
