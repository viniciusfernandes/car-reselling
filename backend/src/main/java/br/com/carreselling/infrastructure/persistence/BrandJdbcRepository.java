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
    public Brand saveBrand(Brand brand) {
        jdbcTemplate.update("""
                INSERT INTO brands (id, name, created_at, updated_at)
                VALUES (?, ?, ?, ?)
                """,
            brand.getId().toString(),
            brand.getName(),
            Timestamp.from(brand.getCreatedAt()),
            brand.getUpdatedAt() == null ? null : Timestamp.from(brand.getUpdatedAt())
        );
        return brand;
    }

    @Override
    public List<Brand> findBrands() {
        return jdbcTemplate.query("""
                SELECT * FROM brands ORDER BY name ASC
                """,
            new BrandRowMapper());
    }

    @Override
    public Optional<Brand> findBrandById(UUID id) {
        List<Brand> result = jdbcTemplate.query("""
                SELECT * FROM brands WHERE id = ?
                """,
            new BrandRowMapper(),
            id.toString());
        return result.stream().findFirst();
    }

    @Override
    public Optional<Brand> findBrandByName(String name) {
        List<Brand> result = jdbcTemplate.query("""
                SELECT * FROM brands WHERE name = ?
                """,
            new BrandRowMapper(),
            name);
        return result.stream().findFirst();
    }

    private static class BrandRowMapper implements RowMapper<Brand> {

        @Override
        public Brand mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = UUID.fromString(rs.getString("id"));
            String name = rs.getString("name");
            Instant createdAt = rs.getTimestamp("created_at").toInstant();
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            return new Brand(
                id,
                name,
                createdAt,
                updatedAt == null ? null : updatedAt.toInstant()
            );
        }
    }
}
