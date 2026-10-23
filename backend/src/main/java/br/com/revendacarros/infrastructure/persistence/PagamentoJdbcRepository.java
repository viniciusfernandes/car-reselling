package br.com.revendacarros.infrastructure.persistence;

import br.com.revendacarros.application.service.model.TotalPagamentoMensal;
import br.com.revendacarros.domain.model.Pagamento;
import br.com.revendacarros.domain.model.TipoPagamento;
import br.com.revendacarros.domain.repository.PagamentoRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PagamentoJdbcRepository implements PagamentoRepository {

    private final JdbcTemplate jdbcTemplate;

    public PagamentoJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Pagamento savePagamento(int empresaId, Pagamento pagamento) {
        jdbcTemplate.update("""
                        INSERT INTO pagamentos
                        (id, empresa_id, tipo_pagamento, descricao, valor, data_pagamento, veiculo_id,
                         observacoes, criado_em, atualizado_em)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                pagamento.id,
                empresaId,
                pagamento.tipoPagamento.name(),
                pagamento.descricao,
                pagamento.valor,
                pagamento.dataPagamento,
                pagamento.veiculoId,
                pagamento.observacoes,
                Timestamp.from(pagamento.criadoEm),
                pagamento.atualizadoEm == null ? null : Timestamp.from(pagamento.atualizadoEm)
        );
        return pagamento;
    }

    @Override
    public Optional<Pagamento> findPagamentoById(int empresaId, UUID id) {
        List<Pagamento> result = jdbcTemplate.query("""
                        SELECT p.*, v.placa
                        FROM pagamentos p
                        LEFT JOIN veiculos v ON v.id = p.veiculo_id AND v.empresa_id = p.empresa_id
                        WHERE p.id = ? AND p.empresa_id = ?
                        """,
                new PagamentoRowMapper(),
                id,
                empresaId);
        return result.stream().findFirst();
    }

    @Override
    public List<Pagamento> findPagamentos(int empresaId, TipoPagamento tipoPagamento, Integer anoPagamento, Integer mesPagamento, String placa) {
        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        StringBuilder sql = new StringBuilder("""
                SELECT p.*, v.placa
                FROM pagamentos p
                LEFT JOIN veiculos v ON v.id = p.veiculo_id AND v.empresa_id = p.empresa_id
                WHERE p.empresa_id = ?
                """);

        if (tipoPagamento != null) {
            sql.append(" AND p.tipo_pagamento = ?");
            params.add(tipoPagamento.name());
        }
        if (anoPagamento != null) {
            sql.append(" AND EXTRACT(YEAR FROM p.data_pagamento) = ?");
            params.add(anoPagamento);
        }
        if (mesPagamento != null) {
            sql.append(" AND EXTRACT(MONTH FROM p.data_pagamento) = ?");
            params.add(mesPagamento);
        }
        if (placa != null && !placa.isBlank()) {
            sql.append(" AND v.placa LIKE ?");
            params.add("%" + placa + "%");
        }
        sql.append(" ORDER BY p.data_pagamento DESC, p.criado_em DESC");

        return jdbcTemplate.query(sql.toString(), new PagamentoRowMapper(), params.toArray());
    }

    @Override
    public List<String> findDescricoesDistintas(int empresaId, TipoPagamento tipoPagamento) {
        return jdbcTemplate.queryForList("""
                        SELECT DISTINCT descricao
                        FROM pagamentos
                        WHERE empresa_id = ?
                          AND tipo_pagamento = ?
                          AND descricao IS NOT NULL
                          AND TRIM(descricao) <> ''
                        ORDER BY descricao
                        """,
                String.class,
                empresaId,
                tipoPagamento.name());
    }

    @Override
    public Pagamento updatePagamento(int empresaId, Pagamento pagamento) {
        jdbcTemplate.update("""
                        UPDATE pagamentos
                        SET tipo_pagamento = ?, descricao = ?, valor = ?, data_pagamento = ?,
                            veiculo_id = ?, observacoes = ?, atualizado_em = ?
                        WHERE id = ? AND empresa_id = ?
                        """,
                pagamento.tipoPagamento.name(),
                pagamento.descricao,
                pagamento.valor,
                pagamento.dataPagamento,
                pagamento.veiculoId,
                pagamento.observacoes,
                Timestamp.from(Instant.now()),
                pagamento.id,
                empresaId
        );
        return pagamento;
    }

    @Override
    public void deletePagamento(int empresaId, UUID id) {
        jdbcTemplate.update("DELETE FROM pagamentos WHERE id = ? AND empresa_id = ?", id, empresaId);
    }

    @Override
    public List<Pagamento> findPagamentosByVeiculoId(int empresaId, UUID veiculoId) {
        return jdbcTemplate.query("""
                        SELECT p.*, v.placa
                        FROM pagamentos p
                        LEFT JOIN veiculos v ON v.id = p.veiculo_id AND v.empresa_id = p.empresa_id
                        WHERE p.veiculo_id = ? AND p.empresa_id = ?
                        ORDER BY p.data_pagamento DESC, p.criado_em DESC
                        """,
                new PagamentoRowMapper(),
                veiculoId,
                empresaId);
    }

    @Override
    public void deletePagamentosByVeiculoId(int empresaId, UUID veiculoId) {
        jdbcTemplate.update("DELETE FROM pagamentos WHERE veiculo_id = ? AND empresa_id = ?", veiculoId, empresaId);
    }

    @Override
    public BigDecimal findValorTotalPagamentos(int empresaId, LocalDate dataInicio, LocalDate dataFim) {
        StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(valor), 0) FROM pagamentos WHERE empresa_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        if (dataInicio != null) {
            sql.append(" AND data_pagamento >= ?");
            params.add(Date.valueOf(dataInicio));
        }
        if (dataFim != null) {
            sql.append(" AND data_pagamento <= ?");
            params.add(Date.valueOf(dataFim));
        }
        BigDecimal result = jdbcTemplate.queryForObject(sql.toString(), BigDecimal.class, params.toArray());
        return result == null ? BigDecimal.ZERO : result;
    }

    @Override
    public List<TotalPagamentoMensal> findTotaisPagamentoMensal(int empresaId, LocalDate dataInicio, LocalDate dataFim) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    CAST(EXTRACT(YEAR FROM data_pagamento) AS INTEGER)  AS ano_pagamento,
                    CAST(EXTRACT(MONTH FROM data_pagamento) AS INTEGER) AS mes_pagamento,
                    SUM(valor)         AS total
                FROM pagamentos
                WHERE empresa_id = ?
                """);
        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        if (dataInicio != null) {
            sql.append(" AND data_pagamento >= ?");
            params.add(Date.valueOf(dataInicio));
        }
        if (dataFim != null) {
            sql.append(" AND data_pagamento <= ?");
            params.add(Date.valueOf(dataFim));
        }
        sql.append(" GROUP BY EXTRACT(YEAR FROM data_pagamento), EXTRACT(MONTH FROM data_pagamento) ORDER BY ano_pagamento, mes_pagamento");
        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> new TotalPagamentoMensal(
                        rs.getInt("ano_pagamento"),
                        rs.getInt("mes_pagamento"),
                        rs.getBigDecimal("total")
                ),
                params.toArray()
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
