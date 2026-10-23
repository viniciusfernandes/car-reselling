package br.com.revendacarros.infrastructure.persistence;

import br.com.revendacarros.domain.model.Documento;
import br.com.revendacarros.domain.model.TipoDocumento;
import br.com.revendacarros.domain.repository.DocumentoRepository;
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
public class DocumentoJdbcRepository implements DocumentoRepository {

    private final JdbcTemplate jdbcTemplate;

    public DocumentoJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Documento saveDocumento(int empresaId, Documento documento) {
        jdbcTemplate.update("""
                        INSERT INTO documentos
                        (id, empresa_id, veiculo_id, tipo_documento, nome_original_arquivo, content_type, tamanho_bytes,
                         storage_key, enviado_em, enviado_por)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                documento.getId(),
                empresaId,
                documento.getVeiculoId(),
                documento.getTipoDocumento().name(),
                documento.getNomeOriginalArquivo(),
                documento.getContentType(),
                documento.getTamanhoBytes(),
                documento.getStorageKey(),
                Timestamp.from(documento.getEnviadoEm()),
                documento.getEnviadoPor()
        );
        return documento;
    }

    @Override
    public Optional<Documento> findDocumentoById(int empresaId, UUID id) {
        List<Documento> result = jdbcTemplate.query("""
                        SELECT * FROM documentos WHERE id = ? AND empresa_id = ?
                        """,
                new DocumentoRowMapper(),
                id,
                empresaId);
        return result.stream().findFirst();
    }

    @Override
    public List<Documento> findDocumentoByVeiculoId(int empresaId, UUID veiculoId) {
        return jdbcTemplate.query("""
                        SELECT * FROM documentos WHERE veiculo_id = ? AND empresa_id = ? ORDER BY enviado_em DESC
                        """,
                new DocumentoRowMapper(),
                veiculoId,
                empresaId);
    }

    @Override
    public List<String> findStorageKeyByVeiculoId(int empresaId, UUID veiculoId) {
        return jdbcTemplate.queryForList("""
                        SELECT storage_key FROM documentos WHERE veiculo_id = ? AND empresa_id = ?""",
                String.class,
                veiculoId,
                empresaId);
    }

    @Override
    public void deleteDocumento(int empresaId, UUID id) {
        jdbcTemplate.update("DELETE FROM documentos WHERE id = ? AND empresa_id = ?", id, empresaId);
    }

    @Override
    public void deleteDocumentoByVeiculoId(int empresaId, UUID veiculoId) {
        jdbcTemplate.update("DELETE FROM documentos WHERE veiculo_id = ? AND empresa_id = ?", veiculoId, empresaId);
    }

    private static class DocumentoRowMapper implements RowMapper<Documento> {

        @Override
        public Documento mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = rs.getObject("id", UUID.class);
            int empresaId = rs.getInt("empresa_id");
            UUID veiculoId = rs.getObject("veiculo_id", UUID.class);
            TipoDocumento tipoDocumento = TipoDocumento.valueOf(rs.getString("tipo_documento"));
            String nomeOriginalArquivo = rs.getString("nome_original_arquivo");
            String contentType = rs.getString("content_type");
            long tamanhoBytes = rs.getLong("tamanho_bytes");
            String storageKey = rs.getString("storage_key");
            Instant enviadoEm = rs.getTimestamp("enviado_em").toInstant();
            String enviadoPor = rs.getString("enviado_por");
            return new Documento(id, empresaId, veiculoId, tipoDocumento, nomeOriginalArquivo, contentType, tamanhoBytes, storageKey, enviadoEm, enviadoPor);
        }
    }
}
