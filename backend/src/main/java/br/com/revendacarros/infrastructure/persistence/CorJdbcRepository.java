package br.com.revendacarros.infrastructure.persistence;

import br.com.revendacarros.domain.model.Cor;
import br.com.revendacarros.domain.repository.CorRepository;
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
public class CorJdbcRepository implements CorRepository {

    private final JdbcTemplate jdbcTemplate;

    public CorJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Cor saveCor(int empresaId, Cor cor) {
        jdbcTemplate.update("""
                INSERT INTO cores (id, empresa_id, nome, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, ?)
                """,
            cor.getId(),
            empresaId,
            cor.getNome(),
            Timestamp.from(cor.getCriadoEm()),
            cor.getAtualizadoEm() == null ? null : Timestamp.from(cor.getAtualizadoEm())
        );
        return cor;
    }

    @Override
    public List<Cor> findCores(int empresaId) {
        return jdbcTemplate.query("""
                SELECT * FROM cores WHERE empresa_id = ? ORDER BY nome ASC
                """,
            new CorRowMapper(),
            empresaId);
    }

    @Override
    public Optional<Cor> findCorByNome(int empresaId, String nome) {
        List<Cor> result = jdbcTemplate.query("""
                SELECT * FROM cores WHERE nome = ? AND empresa_id = ?
                """,
            new CorRowMapper(),
            nome,
            empresaId);
        return result.stream().findFirst();
    }

    private static class CorRowMapper implements RowMapper<Cor> {

        @Override
        public Cor mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = rs.getObject("id", UUID.class);
            int empresaId = rs.getInt("empresa_id");
            String nome = rs.getString("nome");
            Instant criadoEm = rs.getTimestamp("criado_em").toInstant();
            Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
            return new Cor(
                id,
                empresaId,
                nome,
                criadoEm,
                atualizadoEm == null ? null : atualizadoEm.toInstant()
            );
        }
    }
}
