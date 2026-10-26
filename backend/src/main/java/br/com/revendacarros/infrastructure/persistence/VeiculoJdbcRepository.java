package br.com.revendacarros.infrastructure.persistence;

import br.com.revendacarros.application.service.model.CustoTotalVeiculos;
import br.com.revendacarros.application.service.model.FiltroVeiculosDistribuidos;
import br.com.revendacarros.application.service.model.VeiculoDistribuido;
import br.com.revendacarros.application.service.model.VeiculoVendido;
import br.com.revendacarros.domain.model.OrigemFornecedor;
import br.com.revendacarros.domain.model.Veiculo;
import br.com.revendacarros.domain.model.StatusVeiculo;
import br.com.revendacarros.domain.repository.VeiculoRepository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class VeiculoJdbcRepository implements VeiculoRepository {

    private final JdbcTemplate jdbcTemplate;

    public VeiculoJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Veiculo saveVeiculo(int empresaId, Veiculo veiculo) {
        jdbcTemplate.update("""
                        INSERT INTO veiculos
                        (id, empresa_id, placa, renavam, chassi, ano, cor, modelo, marca, marca_id, modelo_id, origem_fornecedor,
                         preco_compra, custo_frete, comissao_compra, preco_venda, valor_fipe,
                         documento_comprovante_pagamento_compra_id,
                         documento_nota_fiscal_compra_id, status, parceiro_atribuido_id, distribuido_em, data_venda,
                         taxa_comissao_venda, criado_em, atualizado_em)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                veiculo.getId(),
                empresaId,
                veiculo.getPlaca(),
                veiculo.getRenavam(),
                veiculo.getChassi(),
                veiculo.getAno(),
                veiculo.getCor(),
                veiculo.getModelo(),
                veiculo.getMarca(),
                veiculo.getMarcaId(),
                veiculo.getModeloId(),
                veiculo.getOrigemFornecedor().name(),
                veiculo.getPrecoCompra(),
                veiculo.getCustoFrete(),
                veiculo.getComissaoCompra(),
                veiculo.getPrecoVenda(),
                veiculo.getValorFipe(),
                veiculo.getDocumentoComprovantePagamentoCompraId(),
                veiculo.getDocumentoNotaFiscalCompraId(),
                veiculo.getStatus().name(),
                veiculo.getParceiroAtribuidoId(),
                veiculo.getDistribuidoEm() == null ? null : Timestamp.from(veiculo.getDistribuidoEm()),
                veiculo.getDataVenda(),
                veiculo.getTaxaComissaoVenda(),
                Timestamp.from(veiculo.getCriadoEm()),
                veiculo.getAtualizadoEm() == null ? null : Timestamp.from(veiculo.getAtualizadoEm())
        );
        return veiculo;
    }

    @Override
    public Optional<Veiculo> findVeiculoById(int empresaId, UUID id) {
        List<Veiculo> result = jdbcTemplate.query("""
                        SELECT * FROM veiculos WHERE id = ? AND empresa_id = ?
                        """,
                new VeiculoRowMapper(),
                id,
                empresaId);
        return result.stream().findFirst();
    }

    @Override
    public Optional<Veiculo> findVeiculoByPlaca(int empresaId, String placa) {
        List<Veiculo> result = jdbcTemplate. query("""
                        SELECT * FROM veiculos WHERE placa = ? AND empresa_id = ?
                        """,
                new VeiculoRowMapper(),
                placa,
                empresaId);
        return result.stream().findFirst();
    }

    @Override
    public Optional<Veiculo> findVeiculoByRenavam(int empresaId, String renavam) {
        List<Veiculo> result = jdbcTemplate.query("""
                        SELECT * FROM veiculos WHERE renavam = ? AND empresa_id = ?
                        """,
                new VeiculoRowMapper(),
                renavam,
                empresaId);
        return result.stream().findFirst();
    }

    @Override
    public Optional<Veiculo> findVeiculoByChassi(int empresaId, String chassi) {
        List<Veiculo> result = jdbcTemplate.query("""
                        SELECT * FROM veiculos WHERE chassi = ? AND empresa_id = ?
                        """,
                new VeiculoRowMapper(),
                chassi,
                empresaId);
        return result.stream().findFirst();
    }

    @Override
    public List<Veiculo> findVeiculoByFilter(int empresaId, StatusVeiculo status, String query, Boolean emServico, int page, int size) {
        int offset = Math.max(page, 0) * Math.max(size, 1);
        StringBuilder sql = new StringBuilder("SELECT * FROM veiculos v WHERE v.empresa_id = ? ");
        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        if (status != null) {
            sql.append("AND v.status = ? ");
            params.add(status.name());
        }
        if (emServico != null) {
            if (emServico) {
                sql.append("AND EXISTS (SELECT 1 FROM servicos s WHERE s.veiculo_id = v.id AND s.empresa_id = ? AND s.data_fim IS NULL) ");
                params.add(empresaId);
            } else {
                sql.append("AND NOT EXISTS (SELECT 1 FROM servicos s WHERE s.veiculo_id = v.id AND s.empresa_id = ? AND s.data_fim IS NULL) ");
                params.add(empresaId);
            }
        }
        if (query != null && !query.isBlank()) {
            sql.append("AND (v.placa LIKE ? OR v.modelo LIKE ? OR v.marca LIKE ?) ");
            String q = "%" + query + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }
        sql.append("ORDER BY v.criado_em DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);
        return jdbcTemplate.query(java.util.Objects.requireNonNull(sql.toString()),
                new VeiculoRowMapper(),
                params.toArray(new Object[0]));
    }

    @Override
    public long countVeiculoByFilter(int empresaId, StatusVeiculo status, String query, Boolean emServico) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM veiculos v WHERE v.empresa_id = ? ");
        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        if (status != null) {
            sql.append("AND v.status = ? ");
            params.add(status.name());
        }
        if (emServico != null) {
            if (Boolean.TRUE.equals(emServico)) {
                sql.append("AND EXISTS (SELECT 1 FROM servicos s WHERE s.veiculo_id = v.id AND s.empresa_id = ? AND s.data_fim IS NULL) ");
                params.add(empresaId);
            } else {
                sql.append("AND NOT EXISTS (SELECT 1 FROM servicos s WHERE s.veiculo_id = v.id AND s.empresa_id = ? AND s.data_fim IS NULL) ");
                params.add(empresaId);
            }
        }
        if (query != null && !query.isBlank()) {
            sql.append("AND (v.placa LIKE ? OR v.modelo LIKE ? OR v.marca LIKE ?) ");
            String q = "%" + query + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }
        Long count = jdbcTemplate.queryForObject(
                java.util.Objects.requireNonNull(sql.toString()),
                Long.class,
                params.toArray(new Object[0])
        );
        return count == null ? 0L : count;
    }

    @Override
    public Veiculo updateVeiculo(int empresaId, Veiculo veiculo) {
        jdbcTemplate.update("""
                        UPDATE veiculos
                        SET renavam = ?, chassi = ?, ano = ?, cor = ?, modelo = ?, marca = ?, marca_id = ?, modelo_id = ?, origem_fornecedor = ?,
                            preco_compra = ?, custo_frete = ?, comissao_compra = ?, preco_venda = ?, valor_fipe = ?,
                            documento_comprovante_pagamento_compra_id = ?, documento_nota_fiscal_compra_id = ?,
                            status = ?, parceiro_atribuido_id = ?, distribuido_em = ?, data_venda = ?,
                            taxa_comissao_venda = ?, atualizado_em = ?
                        WHERE id = ? AND empresa_id = ?
                        """,
                veiculo.getRenavam(),
                veiculo.getChassi(),
                veiculo.getAno(),
                veiculo.getCor(),
                veiculo.getModelo(),
                veiculo.getMarca(),
                veiculo.getMarcaId(),
                veiculo.getModeloId(),
                veiculo.getOrigemFornecedor().name(),
                veiculo.getPrecoCompra(),
                veiculo.getCustoFrete(),
                veiculo.getComissaoCompra(),
                veiculo.getPrecoVenda(),
                veiculo.getValorFipe(),
                veiculo.getDocumentoComprovantePagamentoCompraId(),
                veiculo.getDocumentoNotaFiscalCompraId(),
                veiculo.getStatus().name(),
                veiculo.getParceiroAtribuidoId(),
                veiculo.getDistribuidoEm() == null ? null : Timestamp.from(veiculo.getDistribuidoEm()),
                veiculo.getDataVenda(),
                veiculo.getTaxaComissaoVenda(),
                veiculo.getAtualizadoEm() == null ? Timestamp.from(Instant.now()) : Timestamp.from(veiculo.getAtualizadoEm()),
                veiculo.getId(),
                empresaId
        );
        return veiculo;
    }

    @Override
    public void deleteVeiculo(int empresaId, UUID id) {
        jdbcTemplate.update("DELETE FROM veiculos WHERE id = ? AND empresa_id = ?", id, empresaId);
    }

    @Override
    public BigDecimal findVeiculoTotalServicosByVeiculoId(int empresaId, UUID veiculoId) {
        BigDecimal total = jdbcTemplate.queryForObject("""
                        SELECT COALESCE(SUM(valor_servico), 0) FROM servicos WHERE veiculo_id = ? AND empresa_id = ?
                        """,
                BigDecimal.class,
                veiculoId,
                empresaId);
        return total == null ? BigDecimal.ZERO : total;
    }

    @Override
    public int countVeiculoDocumentosByVeiculoId(int empresaId, UUID veiculoId) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*) FROM documentos WHERE veiculo_id = ? AND empresa_id = ?
                        """,
                Integer.class,
                veiculoId,
                empresaId);
        return count == null ? 0 : count;
    }

    @Override
    public List<VeiculoDistribuido> relatorioVeiculosDistribuidos(int empresaId, FiltroVeiculosDistribuidos filter) {
        StringBuilder sql = new StringBuilder("""
                SELECT p.id AS parceiro_id,
                       p.nome AS nome_parceiro,
                       v.id AS veiculo_id,
                       v.placa,
                       v.marca,
                       v.modelo,
                       v.ano,
                       DATE(COALESCE(v.distribuido_em, v.atualizado_em)) AS distribuido_em,
                       v.preco_compra,
                       COALESCE(v.comissao_compra, 0) AS comissao_compra,
                       v.custo_frete,
                       COALESCE(s.total_servicos, 0) AS total_servicos
                FROM veiculos v
                INNER JOIN parceiros p ON p.id = v.parceiro_atribuido_id AND p.empresa_id = v.empresa_id
                LEFT JOIN (
                    SELECT veiculo_id, SUM(valor_servico) AS total_servicos
                    FROM servicos
                    WHERE empresa_id = ?
                    GROUP BY veiculo_id
                ) s ON s.veiculo_id = v.id
                WHERE v.status = 'DISTRIBUIDO' AND v.empresa_id = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        params.add(empresaId);

        if (filter != null) {
            LocalDate dataInicio = filter.dataInicio();
            LocalDate dataFim = filter.dataFim();
            String marca = normalizeText(filter.marca());
            String modelo = normalizeText(filter.modelo());
            if (dataInicio != null) {
                sql.append(" AND DATE(v.atualizado_em) >= ?");
                params.add(Date.valueOf(dataInicio));
            }
            if (dataFim != null) {
                sql.append(" AND DATE(v.atualizado_em) <= ?");
                params.add(Date.valueOf(dataFim));
            }
            if (marca != null) {
                sql.append(" AND UPPER(v.marca) LIKE ?");
                params.add("%" + marca + "%");
            }
            if (modelo != null) {
                sql.append(" AND UPPER(v.modelo) LIKE ?");
                params.add("%" + modelo + "%");
            }
            if (filter.parceiroId() != null) {
                sql.append(" AND v.parceiro_atribuido_id = ?");
                params.add(filter.parceiroId());
            }
        }

        sql.append(" ORDER BY p.nome, v.placa");

        return jdbcTemplate.query(
                Objects.requireNonNull(sql.toString()),
                new RelatorioRowMapper(),
                params.toArray(new Object[0])
        );
    }

    @Override
    public List<VeiculoVendido> findTotalServicosFromVeiculosVendidos(int empresaId, FiltroVeiculosDistribuidos filter) {
        StringBuilder sql = new StringBuilder("""
                SELECT v.id AS veiculo_id,
                       v.placa,
                       v.marca,
                       v.modelo,
                       v.ano,
                       v.data_venda,
                       v.preco_compra,
                       COALESCE(v.comissao_compra, 0) AS comissao_compra,
                       v.custo_frete,
                       v.preco_venda,
                       COALESCE(s.total_servicos, 0) AS total_servicos,
                       v.taxa_comissao_venda
                FROM veiculos v
                LEFT JOIN (
                    SELECT veiculo_id, SUM(valor_servico) AS total_servicos
                    FROM servicos
                    WHERE empresa_id = ?
                    GROUP BY veiculo_id
                ) s ON s.veiculo_id = v.id
                WHERE v.status = 'VENDIDO' AND v.preco_venda IS NOT NULL AND v.empresa_id = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        params.add(empresaId);
        if (filter != null) {
            LocalDate dataInicio = filter.dataInicio();
            LocalDate dataFim = filter.dataFim();
            String marca = normalizeText(filter.marca());
            String modelo = normalizeText(filter.modelo());
            if (dataInicio != null) {
                sql.append(" AND v.data_venda >= ?");
                params.add(Date.valueOf(dataInicio));
            }
            if (dataFim != null) {
                sql.append(" AND v.data_venda <= ?");
                params.add(Date.valueOf(dataFim));
            }
            if (marca != null) {
                sql.append(" AND UPPER(v.marca) LIKE ?");
                params.add("%" + marca + "%");
            }
            if (modelo != null) {
                sql.append(" AND UPPER(v.modelo) LIKE ?");
                params.add("%" + modelo + "%");
            }
            if (filter.parceiroId() != null) {
                sql.append(" AND v.parceiro_atribuido_id = ?");
                params.add(filter.parceiroId());
            }
        }
        sql.append(" ORDER BY v.data_venda DESC, v.placa");

        return jdbcTemplate.query(
                java.util.Objects.requireNonNull(sql.toString()),
                new VeiculoVendidoMapper(),
                params.toArray(new Object[0])
        );
    }

    @Override
    public CustoTotalVeiculos findVeiculoCustoTotal(int empresaId) {
        return jdbcTemplate.queryForObject("""
                        SELECT
                            COUNT(*) AS total_veiculos,
                            COALESCE(SUM(v.preco_compra + v.custo_frete + v.comissao_compra
                                + COALESCE(svc.total_servicos, 0)), 0) AS custo_total,
                            COALESCE(SUM(v.preco_compra), 0) AS total_preco_compra,
                            COALESCE(SUM(v.comissao_compra), 0) AS total_comissao_compra
                        FROM veiculos v
                        LEFT JOIN (
                            SELECT veiculo_id, SUM(valor_servico) AS total_servicos
                            FROM servicos
                            WHERE empresa_id = ?
                            GROUP BY veiculo_id
                        ) svc ON svc.veiculo_id = v.id
                        WHERE v.status != 'VENDIDO' AND v.empresa_id = ?
                        """,
                (rs, rowNum) -> new CustoTotalVeiculos(
                        rs.getInt("total_veiculos"),
                        rs.getBigDecimal("custo_total"),
                        rs.getBigDecimal("total_preco_compra"),
                        rs.getBigDecimal("total_comissao_compra")
                ),
                empresaId,
                empresaId
        );
    }

    private static class VeiculoVendidoMapper implements RowMapper<VeiculoVendido> {

        @Override
        public VeiculoVendido mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            Date dataVendaDate = rs.getDate("data_venda");
            return new VeiculoVendido(
                    rs.getObject("veiculo_id", UUID.class),
                    rs.getString("placa"),
                    rs.getString("marca"),
                    rs.getString("modelo"),
                    rs.getInt("ano"),
                    dataVendaDate != null ? dataVendaDate.toLocalDate() : null,
                    rs.getBigDecimal("preco_compra"),
                    rs.getBigDecimal("comissao_compra"),
                    rs.getBigDecimal("custo_frete"),
                    rs.getBigDecimal("preco_venda"),
                    rs.getBigDecimal("total_servicos"),
                    rs.getBigDecimal("taxa_comissao_venda")
            );
        }
    }

    private static class RelatorioRowMapper implements RowMapper<VeiculoDistribuido> {

        @Override
        public VeiculoDistribuido mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            return new VeiculoDistribuido(
                    rs.getObject("parceiro_id", UUID.class),
                    rs.getString("nome_parceiro"),
                    rs.getObject("veiculo_id", UUID.class),
                    rs.getString("placa"),
                    rs.getString("marca"),
                    rs.getString("modelo"),
                    rs.getInt("ano"),
                    rs.getDate("distribuido_em") == null ? null : rs.getDate("distribuido_em").toLocalDate(),
                    rs.getBigDecimal("preco_compra"),
                    rs.getBigDecimal("comissao_compra"),
                    rs.getBigDecimal("custo_frete"),
                    rs.getBigDecimal("total_servicos")
            );
        }
    }

    private static class VeiculoRowMapper implements RowMapper<Veiculo> {

        @Override
        public Veiculo mapRow(@org.springframework.lang.NonNull ResultSet rs, int rowNum) throws SQLException {
            UUID id = rs.getObject("id", UUID.class);
            int empresaId = rs.getInt("empresa_id");
            String placa = rs.getString("placa");
            String renavam = rs.getString("renavam");
            String chassi = rs.getString("chassi");
            int ano = rs.getInt("ano");
            String cor = rs.getString("cor");
            String modelo = rs.getString("modelo");
            String marca = rs.getString("marca");
            UUID marcaId = rs.getObject("marca_id", UUID.class);
            UUID modeloId = rs.getObject("modelo_id", UUID.class);
            OrigemFornecedor origemFornecedor = OrigemFornecedor.valueOf(rs.getString("origem_fornecedor"));
            BigDecimal precoCompra = rs.getBigDecimal("preco_compra");
            BigDecimal custoFrete = rs.getBigDecimal("custo_frete");
            BigDecimal comissaoCompra = rs.getBigDecimal("comissao_compra");
            BigDecimal precoVenda = rs.getBigDecimal("preco_venda");
            BigDecimal valorFipe = rs.getBigDecimal("valor_fipe");
            UUID pagamentoComprovanteId = rs.getObject("documento_comprovante_pagamento_compra_id", UUID.class);
            UUID notaFiscalId = rs.getObject("documento_nota_fiscal_compra_id", UUID.class);
            StatusVeiculo status = StatusVeiculo.valueOf(rs.getString("status"));
            UUID parceiroAtribuidoId = rs.getObject("parceiro_atribuido_id", UUID.class);
            Timestamp distribuidoEm = rs.getTimestamp("distribuido_em");
            java.sql.Date dataVendaDate = rs.getDate("data_venda");
            BigDecimal taxaComissaoVenda = rs.getBigDecimal("taxa_comissao_venda");
            Instant criadoEm = rs.getTimestamp("criado_em").toInstant();
            Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
            return new Veiculo(
                    id,
                    empresaId,
                    placa,
                    renavam,
                    chassi,
                    ano,
                    cor,
                    modelo,
                    marca,
                    marcaId,
                    modeloId,
                    origemFornecedor,
                    precoCompra,
                    custoFrete == null ? BigDecimal.ZERO : custoFrete,
                    comissaoCompra == null ? BigDecimal.ZERO : comissaoCompra,
                    precoVenda,
                    valorFipe,
                    pagamentoComprovanteId,
                    notaFiscalId,
                    status,
                    parceiroAtribuidoId,
                    distribuidoEm == null ? null : distribuidoEm.toInstant(),
                    dataVendaDate == null ? null : dataVendaDate.toLocalDate(),
                    taxaComissaoVenda,
                    criadoEm,
                    atualizadoEm == null ? null : atualizadoEm.toInstant()
            );
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toUpperCase();
    }
}
