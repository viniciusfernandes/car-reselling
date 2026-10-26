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

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CorJdbcRepository implements CorRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public CorJdbcRepository(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public Cor saveCor(int empresaId, Cor cor) {
        namedJdbcTemplate.update("""
                        INSERT INTO revenda.cores (id, empresa_id, nome, criado_em, atualizado_em)
                        VALUES (:id, :empresaId, :nome, :criadoEm, :atualizadoEm)
                        """,
                new MapSqlParameterSource()
                        .addValue("id", cor.getId())
                        .addValue("empresaId", empresaId)
                        .addValue("nome", cor.getNome())
                        .addValue("criadoEm", Timestamp.from(cor.getCriadoEm()))
                        .addValue("atualizadoEm", cor.getAtualizadoEm() == null ? null : Timestamp.from(cor.getAtualizadoEm()))
        );
        return cor;
    }

    @Override
    public List<Cor> findCores(int empresaId) {
        return namedJdbcTemplate.query("""
                        SELECT * FROM revenda.cores WHERE empresa_id = :empresaId ORDER BY nome ASC
                        """,
                new MapSqlParameterSource("empresaId", empresaId),
                new CorRowMapper());
    }

    @Override
    public Optional<Cor> findCorByNome(int empresaId, String nome) {
        List<Cor> result = namedJdbcTemplate.query("""
                        SELECT * FROM revenda.cores WHERE nome = :nome AND empresa_id = :empresaId
                        """,
                new MapSqlParameterSource()
                        .addValue("nome", nome)
                        .addValue("empresaId", empresaId),
                new CorRowMapper());
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
