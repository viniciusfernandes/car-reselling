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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class VeiculoJdbcRepository implements VeiculoRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public VeiculoJdbcRepository(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public Veiculo saveVeiculo(int empresaId, Veiculo veiculo) {
        namedJdbcTemplate.update("""
                        INSERT INTO revenda.veiculos
                        (id, empresa_id, placa, renavam, chassi, ano, cor, modelo, marca, marca_id, modelo_id, origem_fornecedor,
                         preco_compra, custo_frete, comissao_compra, preco_venda, valor_fipe,
                         documento_comprovante_pagamento_compra_id,
                         documento_nota_fiscal_compra_id, status, parceiro_atribuido_id, distribuido_em, data_venda,
                         taxa_comissao_venda, criado_em, atualizado_em)
                        VALUES (:id, :empresaId, :placa, :renavam, :chassi, :ano, :cor, :modelo, :marca, :marcaId, :modeloId,
                                :origemFornecedor, :precoCompra, :custoFrete, :comissaoCompra, :precoVenda, :valorFipe,
                                :documentoComprovantePagamentoCompraId, :documentoNotaFiscalCompraId, :status,
                                :parceiroAtribuidoId, :distribuidoEm, :dataVenda, :taxaComissaoVenda, :criadoEm, :atualizadoEm)
                        """,
                new MapSqlParameterSource()
                        .addValue("id", veiculo.getId())
                        .addValue("empresaId", empresaId)
                        .addValue("placa", veiculo.getPlaca())
                        .addValue("renavam", veiculo.getRenavam())
                        .addValue("chassi", veiculo.getChassi())
                        .addValue("ano", veiculo.getAno())
                        .addValue("cor", veiculo.getCor())
                        .addValue("modelo", veiculo.getModelo())
                        .addValue("marca", veiculo.getMarca())
                        .addValue("marcaId", veiculo.getMarcaId())
                        .addValue("modeloId", veiculo.getModeloId())
                        .addValue("origemFornecedor", veiculo.getOrigemFornecedor().name())
                        .addValue("precoCompra", veiculo.getPrecoCompra())
                        .addValue("custoFrete", veiculo.getCustoFrete())
                        .addValue("comissaoCompra", veiculo.getComissaoCompra())
                        .addValue("precoVenda", veiculo.getPrecoVenda())
                        .addValue("valorFipe", veiculo.getValorFipe())
                        .addValue("documentoComprovantePagamentoCompraId", veiculo.getDocumentoComprovantePagamentoCompraId())
                        .addValue("documentoNotaFiscalCompraId", veiculo.getDocumentoNotaFiscalCompraId())
                        .addValue("status", veiculo.getStatus().name())
                        .addValue("parceiroAtribuidoId", veiculo.getParceiroAtribuidoId())
                        .addValue("distribuidoEm", veiculo.getDistribuidoEm() == null ? null : Timestamp.from(veiculo.getDistribuidoEm()))
                        .addValue("dataVenda", veiculo.getDataVenda())
                        .addValue("taxaComissaoVenda", veiculo.getTaxaComissaoVenda())
                        .addValue("criadoEm", Timestamp.from(veiculo.getCriadoEm()))
                        .addValue("atualizadoEm", veiculo.getAtualizadoEm() == null ? null : Timestamp.from(veiculo.getAtualizadoEm()))
        );
        return veiculo;
    }

    @Override
    public Optional<Veiculo> findVeiculoById(int empresaId, UUID id) {
        List<Veiculo> result = namedJdbcTemplate.query("""
                        SELECT * FROM revenda.veiculos WHERE id = :id AND empresa_id = :empresaId
                        """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("empresaId", empresaId),
                new VeiculoRowMapper());
        return result.stream().findFirst();
    }

    @Override
    public Optional<Veiculo> findVeiculoByPlaca(int empresaId, String placa) {
        List<Veiculo> result = namedJdbcTemplate.query("""
                        SELECT * FROM revenda.veiculos WHERE placa = :placa AND empresa_id = :empresaId
                        """,
                new MapSqlParameterSource()
                        .addValue("placa", placa)
                        .addValue("empresaId", empresaId),
                new VeiculoRowMapper());
        return result.stream().findFirst();
    }

    @Override
    public Optional<Veiculo> findVeiculoByRenavam(int empresaId, String renavam) {
        List<Veiculo> result = namedJdbcTemplate.query("""
                        SELECT * FROM revenda.veiculos WHERE renavam = :renavam AND empresa_id = :empresaId
                        """,
                new MapSqlParameterSource()
                        .addValue("renavam", renavam)
                        .addValue("empresaId", empresaId),
                new VeiculoRowMapper());
        return result.stream().findFirst();
    }

    @Override
    public Optional<Veiculo> findVeiculoByChassi(int empresaId, String chassi) {
        List<Veiculo> result = namedJdbcTemplate.query("""
                        SELECT * FROM revenda.veiculos WHERE chassi = :chassi AND empresa_id = :empresaId
                        """,
                new MapSqlParameterSource()
                        .addValue("chassi", chassi)
                        .addValue("empresaId", empresaId),
                new VeiculoRowMapper());
        return result.stream().findFirst();
    }

    @Override
    public List<Veiculo> findVeiculoByFilter(int empresaId, StatusVeiculo status, String query, Boolean emServico, int page, int size) {
        int offset = Math.max(page, 0) * Math.max(size, 1);
        MapSqlParameterSource params = new MapSqlParameterSource("empresaId", empresaId);
        StringBuilder sql = new StringBuilder("SELECT * FROM revenda.veiculos v WHERE v.empresa_id = :empresaId ");
        appendVeiculoFilters(sql, params, status, query, emServico);
        sql.append("ORDER BY v.criado_em DESC LIMIT :limit OFFSET :offset");
        params.addValue("limit", size);
        params.addValue("offset", offset);
        return namedJdbcTemplate.query(sql.toString(), params, new VeiculoRowMapper());
    }

    @Override
    public long countVeiculoByFilter(int empresaId, StatusVeiculo status, String query, Boolean emServico) {
        MapSqlParameterSource params = new MapSqlParameterSource("empresaId", empresaId);
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM revenda.veiculos v WHERE v.empresa_id = :empresaId ");
        appendVeiculoFilters(sql, params, status, query, emServico);
        Long count = namedJdbcTemplate.queryForObject(sql.toString(), params, Long.class);
        return count == null ? 0L : count;
    }

    // Filters shared by the listing and the count queries (alias "v" = revenda.veiculos).
    private void appendVeiculoFilters(StringBuilder sql, MapSqlParameterSource params,
                                      StatusVeiculo status, String query, Boolean emServico) {
        if (status != null) {
            sql.append("AND v.status = :status ");
            params.addValue("status", status.name());
        }
        if (emServico != null) {
            String exists = Boolean.TRUE.equals(emServico) ? "EXISTS" : "NOT EXISTS";
            sql.append("AND ").append(exists)
                    .append(" (SELECT 1 FROM revenda.servicos s WHERE s.veiculo_id = v.id AND s.empresa_id = :empresaId AND s.data_fim IS NULL) ");
        }
        if (query != null && !query.isBlank()) {
            sql.append("AND (v.placa LIKE :query OR v.modelo LIKE :query OR v.marca LIKE :query) ");
            params.addValue("query", "%" + query + "%");
        }
    }

    @Override
    public Veiculo updateVeiculo(int empresaId, Veiculo veiculo) {
        namedJdbcTemplate.update("""
                        UPDATE revenda.veiculos
                        SET renavam = :renavam, chassi = :chassi, ano = :ano, cor = :cor, modelo = :modelo, marca = :marca,
                            marca_id = :marcaId, modelo_id = :modeloId, origem_fornecedor = :origemFornecedor,
                            preco_compra = :precoCompra, custo_frete = :custoFrete, comissao_compra = :comissaoCompra,
                            preco_venda = :precoVenda, valor_fipe = :valorFipe,
                            documento_comprovante_pagamento_compra_id = :documentoComprovantePagamentoCompraId,
                            documento_nota_fiscal_compra_id = :documentoNotaFiscalCompraId,
                            status = :status, parceiro_atribuido_id = :parceiroAtribuidoId, distribuido_em = :distribuidoEm,
                            data_venda = :dataVenda, taxa_comissao_venda = :taxaComissaoVenda, atualizado_em = :atualizadoEm
                        WHERE id = :id AND empresa_id = :empresaId
                        """,
                new MapSqlParameterSource()
                        .addValue("renavam", veiculo.getRenavam())
                        .addValue("chassi", veiculo.getChassi())
                        .addValue("ano", veiculo.getAno())
                        .addValue("cor", veiculo.getCor())
                        .addValue("modelo", veiculo.getModelo())
                        .addValue("marca", veiculo.getMarca())
                        .addValue("marcaId", veiculo.getMarcaId())
                        .addValue("modeloId", veiculo.getModeloId())
                        .addValue("origemFornecedor", veiculo.getOrigemFornecedor().name())
                        .addValue("precoCompra", veiculo.getPrecoCompra())
                        .addValue("custoFrete", veiculo.getCustoFrete())
                        .addValue("comissaoCompra", veiculo.getComissaoCompra())
                        .addValue("precoVenda", veiculo.getPrecoVenda())
                        .addValue("valorFipe", veiculo.getValorFipe())
                        .addValue("documentoComprovantePagamentoCompraId", veiculo.getDocumentoComprovantePagamentoCompraId())
                        .addValue("documentoNotaFiscalCompraId", veiculo.getDocumentoNotaFiscalCompraId())
                        .addValue("status", veiculo.getStatus().name())
                        .addValue("parceiroAtribuidoId", veiculo.getParceiroAtribuidoId())
                        .addValue("distribuidoEm", veiculo.getDistribuidoEm() == null ? null : Timestamp.from(veiculo.getDistribuidoEm()))
                        .addValue("dataVenda", veiculo.getDataVenda())
                        .addValue("taxaComissaoVenda", veiculo.getTaxaComissaoVenda())
                        .addValue("atualizadoEm", veiculo.getAtualizadoEm() == null
                                ? Timestamp.from(Instant.now())
                                : Timestamp.from(veiculo.getAtualizadoEm()))
                        .addValue("id", veiculo.getId())
                        .addValue("empresaId", empresaId)
        );
        return veiculo;
    }

    @Override
    public void deleteVeiculo(int empresaId, UUID id) {
        namedJdbcTemplate.update(
                "DELETE FROM revenda.veiculos WHERE id = :id AND empresa_id = :empresaId",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("empresaId", empresaId));
    }

    @Override
    public BigDecimal findVeiculoTotalServicosByVeiculoId(int empresaId, UUID veiculoId) {
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
    public int countVeiculoDocumentosByVeiculoId(int empresaId, UUID veiculoId) {
        Integer count = namedJdbcTemplate.queryForObject("""
                        SELECT COUNT(*) FROM revenda.documentos WHERE veiculo_id = :veiculoId AND empresa_id = :empresaId
                        """,
                new MapSqlParameterSource()
                        .addValue("veiculoId", veiculoId)
                        .addValue("empresaId", empresaId),
                Integer.class);
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
                FROM revenda.veiculos v
                INNER JOIN revenda.parceiros p ON p.id = v.parceiro_atribuido_id AND p.empresa_id = v.empresa_id
                LEFT JOIN (
                    SELECT veiculo_id, SUM(valor_servico) AS total_servicos
                    FROM revenda.servicos
                    WHERE empresa_id = :empresaId
                    GROUP BY veiculo_id
                ) s ON s.veiculo_id = v.id
                WHERE v.status = 'DISTRIBUIDO' AND v.empresa_id = :empresaId
                """);

        MapSqlParameterSource params = new MapSqlParameterSource("empresaId", empresaId);

        if (filter != null) {
            LocalDate dataInicio = filter.dataInicio();
            LocalDate dataFim = filter.dataFim();
            String marca = normalizeText(filter.marca());
            String modelo = normalizeText(filter.modelo());
            if (dataInicio != null) {
                sql.append(" AND DATE(v.atualizado_em) >= :dataInicio");
                params.addValue("dataInicio", Date.valueOf(dataInicio));
            }
            if (dataFim != null) {
                sql.append(" AND DATE(v.atualizado_em) <= :dataFim");
                params.addValue("dataFim", Date.valueOf(dataFim));
            }
            if (marca != null) {
                sql.append(" AND UPPER(v.marca) LIKE :marca");
                params.addValue("marca", "%" + marca + "%");
            }
            if (modelo != null) {
                sql.append(" AND UPPER(v.modelo) LIKE :modelo");
                params.addValue("modelo", "%" + modelo + "%");
            }
            if (filter.parceiroId() != null) {
                sql.append(" AND v.parceiro_atribuido_id = :parceiroId");
                params.addValue("parceiroId", filter.parceiroId());
            }
        }

        sql.append(" ORDER BY p.nome, v.placa");

        return namedJdbcTemplate.query(sql.toString(), params, new RelatorioRowMapper());
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
                FROM revenda.veiculos v
                LEFT JOIN (
                    SELECT veiculo_id, SUM(valor_servico) AS total_servicos
                    FROM revenda.servicos
                    WHERE empresa_id = :empresaId
                    GROUP BY veiculo_id
                ) s ON s.veiculo_id = v.id
                WHERE v.status = 'VENDIDO' AND v.preco_venda IS NOT NULL AND v.empresa_id = :empresaId
                """);

        MapSqlParameterSource params = new MapSqlParameterSource("empresaId", empresaId);
        if (filter != null) {
            LocalDate dataInicio = filter.dataInicio();
            LocalDate dataFim = filter.dataFim();
            String marca = normalizeText(filter.marca());
            String modelo = normalizeText(filter.modelo());
            if (dataInicio != null) {
                sql.append(" AND v.data_venda >= :dataInicio");
                params.addValue("dataInicio", Date.valueOf(dataInicio));
            }
            if (dataFim != null) {
                sql.append(" AND v.data_venda <= :dataFim");
                params.addValue("dataFim", Date.valueOf(dataFim));
            }
            if (marca != null) {
                sql.append(" AND UPPER(v.marca) LIKE :marca");
                params.addValue("marca", "%" + marca + "%");
            }
            if (modelo != null) {
                sql.append(" AND UPPER(v.modelo) LIKE :modelo");
                params.addValue("modelo", "%" + modelo + "%");
            }
            if (filter.parceiroId() != null) {
                sql.append(" AND v.parceiro_atribuido_id = :parceiroId");
                params.addValue("parceiroId", filter.parceiroId());
            }
        }
        sql.append(" ORDER BY v.data_venda DESC, v.placa");

        return namedJdbcTemplate.query(sql.toString(), params, new VeiculoVendidoMapper());
    }

    @Override
    public CustoTotalVeiculos findVeiculoCustoTotal(int empresaId) {
        return namedJdbcTemplate.queryForObject("""
                        SELECT
                            COUNT(*) AS total_veiculos,
                            COALESCE(SUM(v.preco_compra + v.custo_frete + v.comissao_compra
                                + COALESCE(svc.total_servicos, 0)), 0) AS custo_total,
                            COALESCE(SUM(v.preco_compra), 0) AS total_preco_compra,
                            COALESCE(SUM(v.comissao_compra), 0) AS total_comissao_compra
                        FROM revenda.veiculos v
                        LEFT JOIN (
                            SELECT veiculo_id, SUM(valor_servico) AS total_servicos
                            FROM revenda.servicos
                            WHERE empresa_id = :empresaId
                            GROUP BY veiculo_id
                        ) svc ON svc.veiculo_id = v.id
                        WHERE v.status != 'VENDIDO' AND v.empresa_id = :empresaId
                        """,
                new MapSqlParameterSource("empresaId", empresaId),
                (rs, rowNum) -> new CustoTotalVeiculos(
                        rs.getInt("total_veiculos"),
                        rs.getBigDecimal("custo_total"),
                        rs.getBigDecimal("total_preco_compra"),
                        rs.getBigDecimal("total_comissao_compra")
                )
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
        public Veiculo mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
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
            Date dataVendaDate = rs.getDate("data_venda");
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
