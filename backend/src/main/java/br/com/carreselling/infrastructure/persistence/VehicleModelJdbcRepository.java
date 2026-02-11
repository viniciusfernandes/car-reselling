package br.com.carreselling.infrastructure.persistence;

import br.com.carreselling.domain.model.VehicleModel;
import br.com.carreselling.domain.repository.VehicleModelRepository;
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
public class VehicleModelJdbcRepository implements VehicleModelRepository {

    private final JdbcTemplate jdbcTemplate;

    public VehicleModelJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public VehicleModel saveModel(VehicleModel model) {
        jdbcTemplate.update("""
                INSERT INTO models (id, brand_id, name, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """,
            model.getId().toString(),
            model.getBrandId().toString(),
            model.getName(),
            Timestamp.from(model.getCreatedAt()),
            model.getUpdatedAt() == null ? null : Timestamp.from(model.getUpdatedAt())
        );
        return model;
    }

    @Override
    public List<VehicleModel> findModelsByBrandId(UUID brandId) {
        return jdbcTemplate.query("""
                SELECT * FROM models WHERE brand_id = ? ORDER BY name ASC
                """,
            new VehicleModelRowMapper(),
            brandId.toString());
    }

    @Override
    public Optional<VehicleModel> findModelById(UUID id) {
        List<VehicleModel> result = jdbcTemplate.query("""
                SELECT * FROM models WHERE id = ?
                """,
            new VehicleModelRowMapper(),
            id.toString());
        return result.stream().findFirst();
    }

    @Override
    public Optional<VehicleModel> findModelByBrandIdAndName(UUID brandId, String name) {
        List<VehicleModel> result = jdbcTemplate.query("""
                SELECT * FROM models WHERE brand_id = ? AND name = ?
                """,
            new VehicleModelRowMapper(),
            brandId.toString(),
            name);
        return result.stream().findFirst();
    }

    private static class VehicleModelRowMapper implements RowMapper<VehicleModel> {

        @Override
        public VehicleModel mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = UUID.fromString(rs.getString("id"));
            UUID brandId = UUID.fromString(rs.getString("brand_id"));
            String name = rs.getString("name");
            Instant createdAt = rs.getTimestamp("created_at").toInstant();
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            return new VehicleModel(
                id,
                brandId,
                name,
                createdAt,
                updatedAt == null ? null : updatedAt.toInstant()
            );
        }
    }
}
