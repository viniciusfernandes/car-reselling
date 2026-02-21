package br.com.carreselling.infrastructure.persistence;

import br.com.carreselling.domain.model.Color;
import br.com.carreselling.domain.repository.ColorRepository;
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
public class ColorJdbcRepository implements ColorRepository {

    private final JdbcTemplate jdbcTemplate;

    public ColorJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Color saveColor(Color color) {
        jdbcTemplate.update("""
                INSERT INTO colors (id, name, created_at, updated_at)
                VALUES (?, ?, ?, ?)
                """,
            color.getId().toString(),
            color.getName(),
            Timestamp.from(color.getCreatedAt()),
            color.getUpdatedAt() == null ? null : Timestamp.from(color.getUpdatedAt())
        );
        return color;
    }

    @Override
    public List<Color> findColors() {
        return jdbcTemplate.query("""
                SELECT * FROM colors ORDER BY name ASC
                """,
            new ColorRowMapper());
    }

    @Override
    public Optional<Color> findColorByName(String name) {
        List<Color> result = jdbcTemplate.query("""
                SELECT * FROM colors WHERE name = ?
                """,
            new ColorRowMapper(),
            name);
        return result.stream().findFirst();
    }

    private static class ColorRowMapper implements RowMapper<Color> {

        @Override
        public Color mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = UUID.fromString(rs.getString("id"));
            String name = rs.getString("name");
            Instant createdAt = rs.getTimestamp("created_at").toInstant();
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            return new Color(
                id,
                name,
                createdAt,
                updatedAt == null ? null : updatedAt.toInstant()
            );
        }
    }
}
