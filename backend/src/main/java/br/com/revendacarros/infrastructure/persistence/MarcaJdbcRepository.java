package br.com.revendacarros.infrastructure.persistence;

import br.com.revendacarros.domain.model.Marca;
import br.com.revendacarros.domain.repository.MarcaRepository;
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
public class MarcaJdbcRepository implements MarcaRepository {

    private final JdbcTemplate jdbcTemplate;

    public MarcaJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Marca saveMarca(int empresaId, Marca marca) {
        jdbcTemplate.update("""
                INSERT INTO marcas (id, empresa_id, nome, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, ?)
                """,
            marca.getId(),
            empresaId,
            marca.getNome(),
            Timestamp.from(marca.getCriadoEm()),
            marca.getAtualizadoEm() == null ? null : Timestamp.from(marca.getAtualizadoEm())
        );
        return marca;
    }

    @Override
    public List<Marca> findMarcas(int empresaId) {
        return jdbcTemplate.query("""
                SELECT * FROM marcas WHERE empresa_id = ? ORDER BY nome ASC
                """,
            new MarcaRowMapper(),
            empresaId);
    }

    @Override
    public Optional<Marca> findMarcaById(int empresaId, UUID id) {
        List<Marca> result = jdbcTemplate.query("""
                SELECT * FROM marcas WHERE id = ? AND empresa_id = ?
                """,
            new MarcaRowMapper(),
            id,
            empresaId);
        return result.stream().findFirst();
    }

    @Override
    public Optional<Marca> findMarcaByNome(int empresaId, String nome) {
        List<Marca> result = jdbcTemplate.query("""
                SELECT * FROM marcas WHERE nome = ? AND empresa_id = ?
                """,
            new MarcaRowMapper(),
            nome,
            empresaId);
        return result.stream().findFirst();
    }

    private static class MarcaRowMapper implements RowMapper<Marca> {

        @Override
        public Marca mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = rs.getObject("id", UUID.class);
            int empresaId = rs.getInt("empresa_id");
            String nome = rs.getString("nome");
            Instant criadoEm = rs.getTimestamp("criado_em").toInstant();
            Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
            return new Marca(
                id,
                empresaId,
                nome,
                criadoEm,
                atualizadoEm == null ? null : atualizadoEm.toInstant()
            );
        }
    }
}
