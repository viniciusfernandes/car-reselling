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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ModeloVeiculoJdbcRepository implements ModeloVeiculoRepository {

    private final JdbcTemplate jdbcTemplate;

    public ModeloVeiculoJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ModeloVeiculo saveModelo(int empresaId, ModeloVeiculo modelo) {
        jdbcTemplate.update("""
                INSERT INTO modelos (id, empresa_id, marca_id, nome, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
            modelo.getId(),
            empresaId,
            modelo.getMarcaId(),
            modelo.getNome(),
            Timestamp.from(modelo.getCriadoEm()),
            modelo.getAtualizadoEm() == null ? null : Timestamp.from(modelo.getAtualizadoEm())
        );
        return modelo;
    }

    @Override
    public List<ModeloVeiculo> findModelosByMarcaId(int empresaId, UUID marcaId) {
        return jdbcTemplate.query("""
                SELECT * FROM modelos WHERE marca_id = ? AND empresa_id = ? ORDER BY nome ASC
                """,
            new ModeloVeiculoRowMapper(),
            marcaId,
            empresaId);
    }

    @Override
    public Optional<ModeloVeiculo> findModeloById(int empresaId, UUID id) {
        List<ModeloVeiculo> result = jdbcTemplate.query("""
                SELECT * FROM modelos WHERE id = ? AND empresa_id = ?
                """,
            new ModeloVeiculoRowMapper(),
            id,
            empresaId);
        return result.stream().findFirst();
    }

    @Override
    public Optional<ModeloVeiculo> findModeloByMarcaIdAndNome(int empresaId, UUID marcaId, String nome) {
        List<ModeloVeiculo> result = jdbcTemplate.query("""
                SELECT * FROM modelos WHERE marca_id = ? AND nome = ? AND empresa_id = ?
                """,
            new ModeloVeiculoRowMapper(),
            marcaId,
            nome,
            empresaId);
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
