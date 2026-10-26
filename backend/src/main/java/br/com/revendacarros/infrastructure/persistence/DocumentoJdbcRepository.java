package br.com.revendacarros.infrastructure.persistence;

import br.com.revendacarros.domain.model.Documento;
import br.com.revendacarros.domain.model.TipoDocumento;
import br.com.revendacarros.domain.repository.DocumentoRepository;
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
public class DocumentoJdbcRepository implements DocumentoRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public DocumentoJdbcRepository(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public Documento saveDocumento(int empresaId, Documento documento) {
        namedJdbcTemplate.update("""
                        INSERT INTO revenda.documentos
                        (id, empresa_id, veiculo_id, tipo_documento, nome_original_arquivo, content_type, tamanho_bytes,
                         storage_key, enviado_em, enviado_por)
                        VALUES (:id, :empresaId, :veiculoId, :tipoDocumento, :nomeOriginalArquivo, :contentType, :tamanhoBytes,
                                :storageKey, :enviadoEm, :enviadoPor)
                        """,
                new MapSqlParameterSource()
                        .addValue("id", documento.getId())
                        .addValue("empresaId", empresaId)
                        .addValue("veiculoId", documento.getVeiculoId())
                        .addValue("tipoDocumento", documento.getTipoDocumento().name())
                        .addValue("nomeOriginalArquivo", documento.getNomeOriginalArquivo())
                        .addValue("contentType", documento.getContentType())
                        .addValue("tamanhoBytes", documento.getTamanhoBytes())
                        .addValue("storageKey", documento.getStorageKey())
                        .addValue("enviadoEm", Timestamp.from(documento.getEnviadoEm()))
                        .addValue("enviadoPor", documento.getEnviadoPor())
        );
        return documento;
    }

    @Override
    public Optional<Documento> findDocumentoById(int empresaId, UUID id) {
        List<Documento> result = namedJdbcTemplate.query("""
                        SELECT * FROM revenda.documentos WHERE id = :id AND empresa_id = :empresaId
                        """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("empresaId", empresaId),
                new DocumentoRowMapper());
        return result.stream().findFirst();
    }

    @Override
    public List<Documento> findDocumentoByVeiculoId(int empresaId, UUID veiculoId) {
        return namedJdbcTemplate.query("""
                        SELECT * FROM revenda.documentos
                        WHERE veiculo_id = :veiculoId AND empresa_id = :empresaId
                        ORDER BY enviado_em DESC
                        """,
                new MapSqlParameterSource()
                        .addValue("veiculoId", veiculoId)
                        .addValue("empresaId", empresaId),
                new DocumentoRowMapper());
    }

    @Override
    public List<String> findStorageKeyByVeiculoId(int empresaId, UUID veiculoId) {
        return namedJdbcTemplate.queryForList("""
                        SELECT storage_key FROM revenda.documentos WHERE veiculo_id = :veiculoId AND empresa_id = :empresaId""",
                new MapSqlParameterSource()
                        .addValue("veiculoId", veiculoId)
                        .addValue("empresaId", empresaId),
                String.class);
    }

    @Override
    public void deleteDocumento(int empresaId, UUID id) {
        namedJdbcTemplate.update(
                "DELETE FROM revenda.documentos WHERE id = :id AND empresa_id = :empresaId",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("empresaId", empresaId));
    }

    @Override
    public void deleteDocumentoByVeiculoId(int empresaId, UUID veiculoId) {
        namedJdbcTemplate.update(
                "DELETE FROM revenda.documentos WHERE veiculo_id = :veiculoId AND empresa_id = :empresaId",
                new MapSqlParameterSource()
                        .addValue("veiculoId", veiculoId)
                        .addValue("empresaId", empresaId));
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
