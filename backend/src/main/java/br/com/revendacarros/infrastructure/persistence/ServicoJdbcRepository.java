package br.com.revendacarros.infrastructure.persistence;

import br.com.revendacarros.domain.model.ServicoNoVeiculo;
import br.com.revendacarros.domain.model.TipoServico;
import br.com.revendacarros.domain.repository.ServicoRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public ServicoJdbcRepository(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public ServicoNoVeiculo saveService(int empresaId, ServicoNoVeiculo servicoNoVeiculo) {
        namedJdbcTemplate.update("""
                        INSERT INTO revenda.servicos
                        (id, empresa_id, veiculo_id, tipo_servico, descricao, valor_servico, data_inicio, data_fim, criado_em, atualizado_em)
                        VALUES (:id, :empresaId, :veiculoId, :tipoServico, :descricao, :valorServico, :dataInicio, :dataFim,
                                :criadoEm, :atualizadoEm)
                        """,
                new MapSqlParameterSource()
                        .addValue("id", servicoNoVeiculo.getId())
                        .addValue("empresaId", empresaId)
                        .addValue("veiculoId", servicoNoVeiculo.getVeiculoId())
                        .addValue("tipoServico", servicoNoVeiculo.getTipoServico().name())
                        .addValue("descricao", servicoNoVeiculo.getDescricao())
                        .addValue("valorServico", servicoNoVeiculo.getValorServico())
                        .addValue("dataInicio", servicoNoVeiculo.getDataInicio())
                        .addValue("dataFim", servicoNoVeiculo.getDataFim())
                        .addValue("criadoEm", Timestamp.from(servicoNoVeiculo.getCriadoEm()))
                        .addValue("atualizadoEm", servicoNoVeiculo.getAtualizadoEm() == null ? null : Timestamp.from(servicoNoVeiculo.getAtualizadoEm()))
        );
        return servicoNoVeiculo;
    }

    @Override
    public Optional<ServicoNoVeiculo> findServicoById(int empresaId, UUID id) {
        List<ServicoNoVeiculo> result = namedJdbcTemplate.query("""
                        SELECT * FROM revenda.servicos WHERE id = :id AND empresa_id = :empresaId
                        """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("empresaId", empresaId),
                new ServicoRowMapper());
        return result.stream().findFirst();
    }

    @Override
    public List<ServicoNoVeiculo> findServicoByVeiculoId(int empresaId, UUID veiculoId) {
        return namedJdbcTemplate.query("""
                        SELECT * FROM revenda.servicos
                        WHERE veiculo_id = :veiculoId AND empresa_id = :empresaId
                        ORDER BY criado_em DESC
                        """,
                new MapSqlParameterSource()
                        .addValue("veiculoId", veiculoId)
                        .addValue("empresaId", empresaId),
                new ServicoRowMapper());
    }

    @Override
    public ServicoNoVeiculo updateService(int empresaId, ServicoNoVeiculo servicoNoVeiculo) {
        namedJdbcTemplate.update("""
                        UPDATE revenda.servicos
                        SET tipo_servico = :tipoServico, descricao = :descricao, valor_servico = :valorServico,
                            data_inicio = :dataInicio, data_fim = :dataFim, atualizado_em = :atualizadoEm
                        WHERE id = :id AND empresa_id = :empresaId
                        """,
                new MapSqlParameterSource()
                        .addValue("tipoServico", servicoNoVeiculo.getTipoServico().name())
                        .addValue("descricao", servicoNoVeiculo.getDescricao())
                        .addValue("valorServico", servicoNoVeiculo.getValorServico())
                        .addValue("dataInicio", servicoNoVeiculo.getDataInicio())
                        .addValue("dataFim", servicoNoVeiculo.getDataFim())
                        .addValue("atualizadoEm", servicoNoVeiculo.getAtualizadoEm() == null
                                ? Timestamp.from(Instant.now())
                                : Timestamp.from(servicoNoVeiculo.getAtualizadoEm()))
                        .addValue("id", servicoNoVeiculo.getId())
                        .addValue("empresaId", empresaId)
        );
        return servicoNoVeiculo;
    }

    @Override
    public void deleteService(int empresaId, UUID id) {
        namedJdbcTemplate.update(
                "DELETE FROM revenda.servicos WHERE id = :id AND empresa_id = :empresaId",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("empresaId", empresaId));
    }

    @Override
    public BigDecimal findServicoTotalByVeiculoId(int empresaId, UUID veiculoId) {
        BigDecimal total = namedJdbcTemplate.queryForObject("""
                        SELECT COALESCE(SUM(valor_servico), 0)
                        FROM revenda.servicos
                        WHERE veiculo_id = :veiculoId AND empresa_id = :empresaId
                        """,
                new MapSqlParameterSource()
                        .addValue("veiculoId", veiculoId)
                        .addValue("empresaId", empresaId),
                BigDecimal.class);
        return total == null ? BigDecimal.ZERO : total;
    }

    @Override
    public void deleteServicosByVeiculoId(int empresaId, UUID veiculoId) {
        namedJdbcTemplate.update(
                "DELETE FROM revenda.servicos WHERE veiculo_id = :veiculoId AND empresa_id = :empresaId",
                new MapSqlParameterSource()
                        .addValue("veiculoId", veiculoId)
                        .addValue("empresaId", empresaId));
    }

    @Override
    public boolean existsOpenServicoByVeiculoId(int empresaId, UUID veiculoId) {
        return Boolean.TRUE.equals(namedJdbcTemplate.queryForObject("""
                        SELECT EXISTS (
                            SELECT 1 FROM revenda.servicos
                            WHERE veiculo_id = :veiculoId AND empresa_id = :empresaId
                              AND (data_fim IS NULL OR data_fim > CURRENT_DATE)
                        )
                        """,
                new MapSqlParameterSource()
                        .addValue("veiculoId", veiculoId)
                        .addValue("empresaId", empresaId),
                Boolean.class
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
