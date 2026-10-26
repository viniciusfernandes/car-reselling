package br.com.revendacarros.infrastructure.persistence;

import br.com.revendacarros.domain.model.ModeloVeiculo;
import br.com.revendacarros.domain.repository.ModeloVeiculoRepository;
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
public class ModeloVeiculoJdbcRepository implements ModeloVeiculoRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public ModeloVeiculoJdbcRepository(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public ModeloVeiculo saveModelo(int empresaId, ModeloVeiculo modelo) {
        namedJdbcTemplate.update("""
                INSERT INTO revenda.modelos (id, empresa_id, marca_id, nome, criado_em, atualizado_em)
                VALUES (:id, :empresaId, :marcaId, :nome, :criadoEm, :atualizadoEm)
                """,
            new MapSqlParameterSource()
                .addValue("id", modelo.getId())
                .addValue("empresaId", empresaId)
                .addValue("marcaId", modelo.getMarcaId())
                .addValue("nome", modelo.getNome())
                .addValue("criadoEm", Timestamp.from(modelo.getCriadoEm()))
                .addValue("atualizadoEm", modelo.getAtualizadoEm() == null ? null : Timestamp.from(modelo.getAtualizadoEm()))
        );
        return modelo;
    }

    @Override
    public List<ModeloVeiculo> findModelosByMarcaId(int empresaId, UUID marcaId) {
        return namedJdbcTemplate.query("""
                SELECT * FROM revenda.modelos WHERE marca_id = :marcaId AND empresa_id = :empresaId ORDER BY nome ASC
                """,
            new MapSqlParameterSource()
                .addValue("marcaId", marcaId)
                .addValue("empresaId", empresaId),
            new ModeloVeiculoRowMapper());
    }

    @Override
    public Optional<ModeloVeiculo> findModeloById(int empresaId, UUID id) {
        List<ModeloVeiculo> result = namedJdbcTemplate.query("""
                SELECT * FROM revenda.modelos WHERE id = :id AND empresa_id = :empresaId
                """,
            new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresaId", empresaId),
            new ModeloVeiculoRowMapper());
        return result.stream().findFirst();
    }

    @Override
    public Optional<ModeloVeiculo> findModeloByMarcaIdAndNome(int empresaId, UUID marcaId, String nome) {
        List<ModeloVeiculo> result = namedJdbcTemplate.query("""
                SELECT * FROM revenda.modelos WHERE marca_id = :marcaId AND nome = :nome AND empresa_id = :empresaId
                """,
            new MapSqlParameterSource()
                .addValue("marcaId", marcaId)
                .addValue("nome", nome)
                .addValue("empresaId", empresaId),
            new ModeloVeiculoRowMapper());
        return result.stream().findFirst();
    }

    private static class ModeloVeiculoRowMapper implements RowMapper<ModeloVeiculo> {

        @Override
        public ModeloVeiculo mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = rs.getObject("id", UUID.class);
            int empresaId = rs.getInt("empresa_id");
            UUID marcaId = rs.getObject("marca_id", UUID.class);
            String nome = rs.getString("nome");
            Instant criadoEm = rs.getTimestamp("criado_em").toInstant();
            Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
            return new ModeloVeiculo(
                id,
                empresaId,
                marcaId,
                nome,
                criadoEm,
                atualizadoEm == null ? null : atualizadoEm.toInstant()
            );
        }
    }
}
