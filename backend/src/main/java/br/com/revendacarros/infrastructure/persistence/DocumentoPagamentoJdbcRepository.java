package br.com.revendacarros.infrastructure.persistence;

import br.com.revendacarros.domain.model.DocumentoPagamento;
import br.com.revendacarros.domain.repository.DocumentoPagamentoRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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

    private final JdbcTemplate jdbcTemplate;

    public DocumentoPagamentoJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public DocumentoPagamento saveDocumentoPagamento(int empresaId, DocumentoPagamento documento) {
        jdbcTemplate.update("""
                        INSERT INTO documentos_pagamento
                        (id, empresa_id, pagamento_id, nome_original_arquivo, content_type, tamanho_bytes,
                         storage_key, enviado_em, enviado_por)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                documento.id,
                empresaId,
                documento.pagamentoId,
                documento.nomeOriginalArquivo,
                documento.contentType,
                documento.tamanhoBytes,
                documento.storageKey,
                Timestamp.from(documento.enviadoEm),
                documento.enviadoPor
        );
        return documento;
    }

    @Override
    public Optional<DocumentoPagamento> findDocumentoPagamentoById(int empresaId, UUID id) {
        List<DocumentoPagamento> result = jdbcTemplate.query("""
                        SELECT * FROM documentos_pagamento WHERE id = ? AND empresa_id = ?
                        """,
                new DocumentoPagamentoRowMapper(),
                id,
                empresaId);
        return result.stream().findFirst();
    }

    @Override
    public List<DocumentoPagamento> findDocumentosPagamentoByPagamentoId(int empresaId, UUID pagamentoId) {
        return jdbcTemplate.query("""
                        SELECT * FROM documentos_pagamento WHERE pagamento_id = ? AND empresa_id = ? ORDER BY enviado_em DESC
                        """,
                new DocumentoPagamentoRowMapper(),
                pagamentoId,
                empresaId);
    }

    @Override
    public void deleteDocumentoPagamento(int empresaId, UUID id) {
        jdbcTemplate.update("DELETE FROM documentos_pagamento WHERE id = ? AND empresa_id = ?", id, empresaId);
    }

    @Override
    public void deleteDocumentosPagamentoByPagamentoId(int empresaId, UUID pagamentoId) {
        jdbcTemplate.update("DELETE FROM documentos_pagamento WHERE pagamento_id = ? AND empresa_id = ?", pagamentoId, empresaId);
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
