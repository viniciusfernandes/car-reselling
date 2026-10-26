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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MarcaJdbcRepository implements MarcaRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public MarcaJdbcRepository(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public Marca saveMarca(int empresaId, Marca marca) {
        namedJdbcTemplate.update("""
                INSERT INTO revenda.marcas (id, empresa_id, nome, criado_em, atualizado_em)
                VALUES (:id, :empresaId, :nome, :criadoEm, :atualizadoEm)
                """,
            new MapSqlParameterSource()
                .addValue("id", marca.getId())
                .addValue("empresaId", empresaId)
                .addValue("nome", marca.getNome())
                .addValue("criadoEm", Timestamp.from(marca.getCriadoEm()))
                .addValue("atualizadoEm", marca.getAtualizadoEm() == null ? null : Timestamp.from(marca.getAtualizadoEm()))
        );
        return marca;
    }

    @Override
    public List<Marca> findMarcas(int empresaId) {
        return namedJdbcTemplate.query("""
                SELECT * FROM revenda.marcas WHERE empresa_id = :empresaId ORDER BY nome ASC
                """,
            new MapSqlParameterSource("empresaId", empresaId),
            new MarcaRowMapper());
    }

    @Override
    public Optional<Marca> findMarcaById(int empresaId, UUID id) {
        List<Marca> result = namedJdbcTemplate.query("""
                SELECT * FROM revenda.marcas WHERE id = :id AND empresa_id = :empresaId
                """,
            new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresaId", empresaId),
            new MarcaRowMapper());
        return result.stream().findFirst();
    }

    @Override
    public Optional<Marca> findMarcaByNome(int empresaId, String nome) {
        List<Marca> result = namedJdbcTemplate.query("""
                SELECT * FROM revenda.marcas WHERE nome = :nome AND empresa_id = :empresaId
                """,
            new MapSqlParameterSource()
                .addValue("nome", nome)
                .addValue("empresaId", empresaId),
            new MarcaRowMapper());
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
