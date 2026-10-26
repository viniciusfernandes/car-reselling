package br.com.revendacarros.infrastructure.persistence;

import br.com.revendacarros.domain.model.DocumentoPagamento;
import br.com.revendacarros.domain.repository.DocumentoPagamentoRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DocumentoPagamentoJdbcRepository implements DocumentoPagamentoRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public DocumentoPagamentoJdbcRepository(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public DocumentoPagamento saveDocumentoPagamento(int empresaId, DocumentoPagamento documento) {
        namedJdbcTemplate.update("""
                        INSERT INTO revenda.documentos_pagamento
                        (id, empresa_id, pagamento_id, nome_original_arquivo, content_type, tamanho_bytes,
                         storage_key, enviado_em, enviado_por)
                        VALUES (:id, :empresaId, :pagamentoId, :nomeOriginalArquivo, :contentType, :tamanhoBytes,
                                :storageKey, :enviadoEm, :enviadoPor)
                        """,
                new MapSqlParameterSource()
                        .addValue("id", documento.id)
                        .addValue("empresaId", empresaId)
                        .addValue("pagamentoId", documento.pagamentoId)
                        .addValue("nomeOriginalArquivo", documento.nomeOriginalArquivo)
                        .addValue("contentType", documento.contentType)
                        .addValue("tamanhoBytes", documento.tamanhoBytes)
                        .addValue("storageKey", documento.storageKey)
                        .addValue("enviadoEm", Timestamp.from(documento.enviadoEm))
                        .addValue("enviadoPor", documento.enviadoPor)
        );
        return documento;
    }

    @Override
    public Optional<DocumentoPagamento> findDocumentoPagamentoById(int empresaId, UUID id) {
        List<DocumentoPagamento> result = namedJdbcTemplate.query("""
                        SELECT * FROM revenda.documentos_pagamento WHERE id = :id AND empresa_id = :empresaId
                        """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("empresaId", empresaId),
                new DocumentoPagamentoRowMapper());
        return result.stream().findFirst();
    }

    @Override
    public List<DocumentoPagamento> findDocumentosPagamentoByPagamentoId(int empresaId, UUID pagamentoId) {
        return namedJdbcTemplate.query("""
                        SELECT * FROM revenda.documentos_pagamento
                        WHERE pagamento_id = :pagamentoId AND empresa_id = :empresaId
                        ORDER BY enviado_em DESC
                        """,
                new MapSqlParameterSource()
                        .addValue("pagamentoId", pagamentoId)
                        .addValue("empresaId", empresaId),
                new DocumentoPagamentoRowMapper());
    }

    @Override
    public void deleteDocumentoPagamento(int empresaId, UUID id) {
        namedJdbcTemplate.update(
                "DELETE FROM revenda.documentos_pagamento WHERE id = :id AND empresa_id = :empresaId",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("empresaId", empresaId));
    }

    @Override
    public void deleteDocumentosPagamentoByPagamentoId(int empresaId, UUID pagamentoId) {
        namedJdbcTemplate.update(
                "DELETE FROM revenda.documentos_pagamento WHERE pagamento_id = :pagamentoId AND empresa_id = :empresaId",
                new MapSqlParameterSource()
                        .addValue("pagamentoId", pagamentoId)
                        .addValue("empresaId", empresaId));
    }

    private static class DocumentoPagamentoRowMapper implements RowMapper<DocumentoPagamento> {

        @Override
        public DocumentoPagamento mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = rs.getObject("id", UUID.class);
            int empresaId = rs.getInt("empresa_id");
            UUID pagamentoId = rs.getObject("pagamento_id", UUID.class);
            String nomeOriginalArquivo = rs.getString("nome_original_arquivo");
            String contentType = rs.getString("content_type");
            long tamanhoBytes = rs.getLong("tamanho_bytes");
            String storageKey = rs.getString("storage_key");
            Instant enviadoEm = rs.getTimestamp("enviado_em").toInstant();
            String enviadoPor = rs.getString("enviado_por");
            return new DocumentoPagamento(id, empresaId, pagamentoId, nomeOriginalArquivo, contentType,
                    tamanhoBytes, storageKey, enviadoEm, enviadoPor);
        }
    }
}
