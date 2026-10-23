package br.com.revendacarros.infrastructure.persistence;

import br.com.revendacarros.domain.model.HistoricoVeiculoEmServico;
import br.com.revendacarros.domain.repository.HistoricoVeiculoEmServicoRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class HistoricoVeiculoEmServicoJdbcRepository implements HistoricoVeiculoEmServicoRepository {

    private final JdbcTemplate jdbcTemplate;

    public HistoricoVeiculoEmServicoJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(int empresaId, HistoricoVeiculoEmServico historico) {
        jdbcTemplate.update("""
                        INSERT INTO historico_veiculo_em_servico (id, empresa_id, veiculo_id, em_servico, alterado_em)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                historico.id(),
                empresaId,
                historico.veiculoId(),
                historico.emServico(),
                Timestamp.from(historico.alteradoEm())
        );
    }

    @Override
    public List<HistoricoVeiculoEmServico> findByVeiculoId(int empresaId, UUID veiculoId) {
        return jdbcTemplate.query("""
                        SELECT id, empresa_id, veiculo_id, em_servico, alterado_em
                        FROM historico_veiculo_em_servico
                        WHERE veiculo_id = ? AND empresa_id = ?
                        ORDER BY alterado_em ASC
                        """,
                new HistoricoRowMapper(),
                veiculoId,
                empresaId
        );
    }

    @Override
    public void deleteByVeiculoId(int empresaId, UUID veiculoId) {
        jdbcTemplate.update(
                "DELETE FROM historico_veiculo_em_servico WHERE veiculo_id = ? AND empresa_id = ?",
                veiculoId,
                empresaId
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
