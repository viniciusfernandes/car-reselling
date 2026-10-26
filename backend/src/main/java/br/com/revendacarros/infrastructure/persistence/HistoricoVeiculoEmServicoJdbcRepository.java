package br.com.revendacarros.infrastructure.persistence;

import br.com.revendacarros.domain.model.HistoricoVeiculoEmServico;
import br.com.revendacarros.domain.repository.HistoricoVeiculoEmServicoRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class HistoricoVeiculoEmServicoJdbcRepository implements HistoricoVeiculoEmServicoRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public HistoricoVeiculoEmServicoJdbcRepository(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public void save(int empresaId, HistoricoVeiculoEmServico historico) {
        namedJdbcTemplate.update("""
                        INSERT INTO revenda.historico_veiculo_em_servico (id, empresa_id, veiculo_id, em_servico, alterado_em)
                        VALUES (:id, :empresaId, :veiculoId, :emServico, :alteradoEm)
                        """,
                new MapSqlParameterSource()
                        .addValue("id", historico.id())
                        .addValue("empresaId", empresaId)
                        .addValue("veiculoId", historico.veiculoId())
                        .addValue("emServico", historico.emServico())
                        .addValue("alteradoEm", Timestamp.from(historico.alteradoEm()))
        );
    }

    @Override
    public List<HistoricoVeiculoEmServico> findByVeiculoId(int empresaId, UUID veiculoId) {
        return namedJdbcTemplate.query("""
                        SELECT id, empresa_id, veiculo_id, em_servico, alterado_em
                        FROM revenda.historico_veiculo_em_servico
                        WHERE veiculo_id = :veiculoId AND empresa_id = :empresaId
                        ORDER BY alterado_em ASC
                        """,
                new MapSqlParameterSource()
                        .addValue("veiculoId", veiculoId)
                        .addValue("empresaId", empresaId),
                new HistoricoRowMapper()
        );
    }

    @Override
    public void deleteByVeiculoId(int empresaId, UUID veiculoId) {
        namedJdbcTemplate.update(
                "DELETE FROM revenda.historico_veiculo_em_servico WHERE veiculo_id = :veiculoId AND empresa_id = :empresaId",
                new MapSqlParameterSource()
                        .addValue("veiculoId", veiculoId)
                        .addValue("empresaId", empresaId)
        );
    }

    private static class HistoricoRowMapper implements RowMapper<HistoricoVeiculoEmServico> {

        @Override
        public HistoricoVeiculoEmServico mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            return new HistoricoVeiculoEmServico(
                    rs.getObject("id", UUID.class),
                    rs.getInt("empresa_id"),
                    rs.getObject("veiculo_id", UUID.class),
                    rs.getBoolean("em_servico"),
                    rs.getTimestamp("alterado_em").toInstant()
            );
        }
    }
}
