package br.com.revendacarros.infrastructure.persistence;

import br.com.revendacarros.application.service.model.TotalPagamentoMensal;
import br.com.revendacarros.domain.model.Pagamento;
import br.com.revendacarros.domain.model.TipoPagamento;
import br.com.revendacarros.domain.repository.PagamentoRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PagamentoJdbcRepository implements PagamentoRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public PagamentoJdbcRepository(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public Pagamento savePagamento(int empresaId, Pagamento pagamento) {
        namedJdbcTemplate.update("""
                        INSERT INTO revenda.pagamentos
                        (id, empresa_id, tipo_pagamento, descricao, valor, data_pagamento, veiculo_id,
                         observacoes, criado_em, atualizado_em)
                        VALUES (:id, :empresaId, :tipoPagamento, :descricao, :valor, :dataPagamento, :veiculoId,
                                :observacoes, :criadoEm, :atualizadoEm)
                        """,
                new MapSqlParameterSource()
                        .addValue("id", pagamento.id)
                        .addValue("empresaId", empresaId)
                        .addValue("tipoPagamento", pagamento.tipoPagamento.name())
                        .addValue("descricao", pagamento.descricao)
                        .addValue("valor", pagamento.valor)
                        .addValue("dataPagamento", pagamento.dataPagamento)
                        .addValue("veiculoId", pagamento.veiculoId)
                        .addValue("observacoes", pagamento.observacoes)
                        .addValue("criadoEm", Timestamp.from(pagamento.criadoEm))
                        .addValue("atualizadoEm", pagamento.atualizadoEm == null ? null : Timestamp.from(pagamento.atualizadoEm))
        );
        return pagamento;
    }

    @Override
    public Optional<Pagamento> findPagamentoById(int empresaId, UUID id) {
        List<Pagamento> result = namedJdbcTemplate.query("""
                        SELECT p.*, v.placa
                        FROM revenda.pagamentos p
                        LEFT JOIN revenda.veiculos v ON v.id = p.veiculo_id AND v.empresa_id = p.empresa_id
                        WHERE p.id = :id AND p.empresa_id = :empresaId
                        """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("empresaId", empresaId),
                new PagamentoRowMapper());
        return result.stream().findFirst();
    }

    @Override
    public List<Pagamento> findPagamentos(int empresaId, TipoPagamento tipoPagamento, Integer anoPagamento, Integer mesPagamento, String placa) {
        MapSqlParameterSource params = new MapSqlParameterSource("empresaId", empresaId);
        StringBuilder sql = new StringBuilder("""
                SELECT p.*, v.placa
                FROM revenda.pagamentos p
                LEFT JOIN revenda.veiculos v ON v.id = p.veiculo_id AND v.empresa_id = p.empresa_id
                WHERE p.empresa_id = :empresaId
                """);

        if (tipoPagamento != null) {
            sql.append(" AND p.tipo_pagamento = :tipoPagamento");
            params.addValue("tipoPagamento", tipoPagamento.name());
        }
        if (anoPagamento != null) {
            sql.append(" AND EXTRACT(YEAR FROM p.data_pagamento) = :anoPagamento");
            params.addValue("anoPagamento", anoPagamento);
        }
        if (mesPagamento != null) {
            sql.append(" AND EXTRACT(MONTH FROM p.data_pagamento) = :mesPagamento");
            params.addValue("mesPagamento", mesPagamento);
        }
        if (placa != null && !placa.isBlank()) {
            sql.append(" AND v.placa LIKE :placa");
            params.addValue("placa", "%" + placa + "%");
        }
        sql.append(" ORDER BY p.data_pagamento DESC, p.criado_em DESC");

        return namedJdbcTemplate.query(sql.toString(), params, new PagamentoRowMapper());
    }

    @Override
    public List<String> findDescricoesDistintas(int empresaId, TipoPagamento tipoPagamento) {
        return namedJdbcTemplate.queryForList("""
                        SELECT DISTINCT descricao
                        FROM revenda.pagamentos
                        WHERE empresa_id = :empresaId
                          AND tipo_pagamento = :tipoPagamento
                          AND descricao IS NOT NULL
                          AND TRIM(descricao) <> ''
                        ORDER BY descricao
                        """,
                new MapSqlParameterSource()
                        .addValue("empresaId", empresaId)
                        .addValue("tipoPagamento", tipoPagamento.name()),
                String.class);
    }

    @Override
    public Pagamento updatePagamento(int empresaId, Pagamento pagamento) {
        namedJdbcTemplate.update("""
                        UPDATE revenda.pagamentos
                        SET tipo_pagamento = :tipoPagamento, descricao = :descricao, valor = :valor,
                            data_pagamento = :dataPagamento, veiculo_id = :veiculoId, observacoes = :observacoes,
                            atualizado_em = :atualizadoEm
                        WHERE id = :id AND empresa_id = :empresaId
                        """,
                new MapSqlParameterSource()
                        .addValue("tipoPagamento", pagamento.tipoPagamento.name())
                        .addValue("descricao", pagamento.descricao)
                        .addValue("valor", pagamento.valor)
                        .addValue("dataPagamento", pagamento.dataPagamento)
                        .addValue("veiculoId", pagamento.veiculoId)
                        .addValue("observacoes", pagamento.observacoes)
                        .addValue("atualizadoEm", Timestamp.from(Instant.now()))
                        .addValue("id", pagamento.id)
                        .addValue("empresaId", empresaId)
        );
        return pagamento;
    }

    @Override
    public void deletePagamento(int empresaId, UUID id) {
        namedJdbcTemplate.update(
                "DELETE FROM revenda.pagamentos WHERE id = :id AND empresa_id = :empresaId",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("empresaId", empresaId));
    }

    @Override
    public List<Pagamento> findPagamentosByVeiculoId(int empresaId, UUID veiculoId) {
        return namedJdbcTemplate.query("""
                        SELECT p.*, v.placa
                        FROM revenda.pagamentos p
                        LEFT JOIN revenda.veiculos v ON v.id = p.veiculo_id AND v.empresa_id = p.empresa_id
                        WHERE p.veiculo_id = :veiculoId AND p.empresa_id = :empresaId
                        ORDER BY p.data_pagamento DESC, p.criado_em DESC
                        """,
                new MapSqlParameterSource()
                        .addValue("veiculoId", veiculoId)
                        .addValue("empresaId", empresaId),
                new PagamentoRowMapper());
    }

    @Override
    public void deletePagamentosByVeiculoId(int empresaId, UUID veiculoId) {
        namedJdbcTemplate.update(
                "DELETE FROM revenda.pagamentos WHERE veiculo_id = :veiculoId AND empresa_id = :empresaId",
                new MapSqlParameterSource()
                        .addValue("veiculoId", veiculoId)
                        .addValue("empresaId", empresaId));
    }

    @Override
    public BigDecimal findValorTotalPagamentos(int empresaId, LocalDate dataInicio, LocalDate dataFim) {
        MapSqlParameterSource params = new MapSqlParameterSource("empresaId", empresaId);
        StringBuilder sql = new StringBuilder(
                "SELECT COALESCE(SUM(valor), 0) FROM revenda.pagamentos WHERE empresa_id = :empresaId");
        if (dataInicio != null) {
            sql.append(" AND data_pagamento >= :dataInicio");
            params.addValue("dataInicio", Date.valueOf(dataInicio));
        }
        if (dataFim != null) {
            sql.append(" AND data_pagamento <= :dataFim");
            params.addValue("dataFim", Date.valueOf(dataFim));
        }
        BigDecimal result = namedJdbcTemplate.queryForObject(sql.toString(), params, BigDecimal.class);
        return result == null ? BigDecimal.ZERO : result;
    }

    @Override
    public List<TotalPagamentoMensal> findTotaisPagamentoMensal(int empresaId, LocalDate dataInicio, LocalDate dataFim) {
        MapSqlParameterSource params = new MapSqlParameterSource("empresaId", empresaId);
        StringBuilder sql = new StringBuilder("""
                SELECT
                    CAST(EXTRACT(YEAR FROM data_pagamento) AS INTEGER)  AS ano_pagamento,
                    CAST(EXTRACT(MONTH FROM data_pagamento) AS INTEGER) AS mes_pagamento,
                    SUM(valor)         AS total
                FROM revenda.pagamentos
                WHERE empresa_id = :empresaId
                """);
        if (dataInicio != null) {
            sql.append(" AND data_pagamento >= :dataInicio");
            params.addValue("dataInicio", Date.valueOf(dataInicio));
        }
        if (dataFim != null) {
            sql.append(" AND data_pagamento <= :dataFim");
            params.addValue("dataFim", Date.valueOf(dataFim));
        }
        sql.append(" GROUP BY EXTRACT(YEAR FROM data_pagamento), EXTRACT(MONTH FROM data_pagamento) ORDER BY ano_pagamento, mes_pagamento");
        return namedJdbcTemplate.query(sql.toString(),
                params,
                (rs, rowNum) -> new TotalPagamentoMensal(
                        rs.getInt("ano_pagamento"),
                        rs.getInt("mes_pagamento"),
                        rs.getBigDecimal("total")
                )
        );
    }

    private static class PagamentoRowMapper implements RowMapper<Pagamento> {

        @Override
        public Pagamento mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = rs.getObject("id", UUID.class);
            int empresaId = rs.getInt("empresa_id");
            TipoPagamento tipoPagamento = TipoPagamento.valueOf(rs.getString("tipo_pagamento"));
            String descricao = rs.getString("descricao");
            BigDecimal valor = rs.getBigDecimal("valor");
            Date dataPagamentoSql = rs.getDate("data_pagamento");
            LocalDate dataPagamento = dataPagamentoSql == null ? null : dataPagamentoSql.toLocalDate();
            UUID veiculoId = rs.getObject("veiculo_id", UUID.class);
            String observacoes = rs.getString("observacoes");
            Instant criadoEm = rs.getTimestamp("criado_em").toInstant();
            Timestamp atualizadoEmTs = rs.getTimestamp("atualizado_em");
            Instant atualizadoEm = atualizadoEmTs == null ? null : atualizadoEmTs.toInstant();
            Pagamento pagamento = new Pagamento(id, empresaId, tipoPagamento, descricao, valor, dataPagamento,
                    veiculoId, observacoes, criadoEm, atualizadoEm);
            String placa = rs.getString("placa");
            if (placa != null) {
                pagamento.veiculoPlaca = placa;
            }
            return pagamento;
        }
    }
}
