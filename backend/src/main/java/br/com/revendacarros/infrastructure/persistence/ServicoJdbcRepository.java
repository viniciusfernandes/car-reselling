package br.com.revendacarros.infrastructure.persistence;

import br.com.revendacarros.domain.model.ServicoNoVeiculo;
import br.com.revendacarros.domain.model.TipoServico;
import br.com.revendacarros.domain.repository.ServicoRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ServicoJdbcRepository implements ServicoRepository {

    private final JdbcTemplate jdbcTemplate;

    public ServicoJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ServicoNoVeiculo saveService(int empresaId, ServicoNoVeiculo servicoNoVeiculo) {
        jdbcTemplate.update("""
                        INSERT INTO servicos
                        (id, empresa_id, veiculo_id, tipo_servico, descricao, valor_servico, data_inicio, data_fim, criado_em, atualizado_em)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                servicoNoVeiculo.getId(),
                empresaId,
                servicoNoVeiculo.getVeiculoId(),
                servicoNoVeiculo.getTipoServico().name(),
                servicoNoVeiculo.getDescricao(),
                servicoNoVeiculo.getValorServico(),
                servicoNoVeiculo.getDataInicio(),
                servicoNoVeiculo.getDataFim(),
                Timestamp.from(servicoNoVeiculo.getCriadoEm()),
                servicoNoVeiculo.getAtualizadoEm() == null ? null : Timestamp.from(servicoNoVeiculo.getAtualizadoEm())
        );
        return servicoNoVeiculo;
    }

    @Override
    public Optional<ServicoNoVeiculo> findServicoById(int empresaId, UUID id) {
        List<ServicoNoVeiculo> result = jdbcTemplate.query("""
                        SELECT * FROM servicos WHERE id = ? AND empresa_id = ?
                        """,
                new ServicoRowMapper(),
                id,
                empresaId);
        return result.stream().findFirst();
    }

    @Override
    public List<ServicoNoVeiculo> findServicoByVeiculoId(int empresaId, UUID veiculoId) {
        return jdbcTemplate.query("""
                        SELECT * FROM servicos WHERE veiculo_id = ? AND empresa_id = ? ORDER BY criado_em DESC
                        """,
                new ServicoRowMapper(),
                veiculoId,
                empresaId);
    }

    @Override
    public ServicoNoVeiculo updateService(int empresaId, ServicoNoVeiculo servicoNoVeiculo) {
        jdbcTemplate.update("""
                        UPDATE servicos
                        SET tipo_servico = ?, descricao = ?, valor_servico = ?,
                            data_inicio = ?, data_fim = ?, atualizado_em = ?
                        WHERE id = ? AND empresa_id = ?
                        """,
                servicoNoVeiculo.getTipoServico().name(),
                servicoNoVeiculo.getDescricao(),
                servicoNoVeiculo.getValorServico(),
                servicoNoVeiculo.getDataInicio(),
                servicoNoVeiculo.getDataFim(),
                servicoNoVeiculo.getAtualizadoEm() == null ? Timestamp.from(Instant.now()) : Timestamp.from(servicoNoVeiculo.getAtualizadoEm()),
                servicoNoVeiculo.getId(),
                empresaId
        );
        return servicoNoVeiculo;
    }

    @Override
    public void deleteService(int empresaId, UUID id) {
        jdbcTemplate.update("DELETE FROM servicos WHERE id = ? AND empresa_id = ?", id, empresaId);
    }

    @Override
    public BigDecimal findServicoTotalByVeiculoId(int empresaId, UUID veiculoId) {
        BigDecimal total = jdbcTemplate.queryForObject("""
                        SELECT COALESCE(SUM(valor_servico), 0) FROM servicos WHERE veiculo_id = ? AND empresa_id = ?
                        """,
                new Object[]{veiculoId, empresaId},
                BigDecimal.class);
        return total == null ? BigDecimal.ZERO : total;
    }

    @Override
    public void deleteServicosByVeiculoId(int empresaId, UUID veiculoId) {
        jdbcTemplate.update("DELETE FROM servicos WHERE veiculo_id = ? AND empresa_id = ?", veiculoId, empresaId);
    }

    @Override
    public boolean existsOpenServicoByVeiculoId(int empresaId, UUID veiculoId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM servicos WHERE veiculo_id = ? AND empresa_id = ? AND (data_fim IS NULL OR data_fim > CURRENT_DATE))",
                Boolean.class,
                veiculoId,
                empresaId
        ));
    }

    private static class ServicoRowMapper implements RowMapper<ServicoNoVeiculo> {

        @Override
        public ServicoNoVeiculo mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = rs.getObject("id", UUID.class);
            int empresaId = rs.getInt("empresa_id");
            UUID veiculoId = rs.getObject("veiculo_id", UUID.class);
            TipoServico tipoServico = TipoServico.valueOf(rs.getString("tipo_servico"));
            String descricao = rs.getString("descricao");
            BigDecimal value = rs.getBigDecimal("valor_servico");
            LocalDate dataInicio = rs.getDate("data_inicio") == null ? null : rs.getDate("data_inicio").toLocalDate();
            LocalDate dataFim = rs.getDate("data_fim") == null ? null : rs.getDate("data_fim").toLocalDate();
            Instant criadoEm = rs.getTimestamp("criado_em").toInstant();
            Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
            return new ServicoNoVeiculo(
                    id,
                    empresaId,
                    veiculoId,
                    tipoServico,
                    descricao,
                    value,
                    dataInicio,
                    dataFim,
                    criadoEm,
                    atualizadoEm == null ? null : atualizadoEm.toInstant()
            );
        }
    }
}
