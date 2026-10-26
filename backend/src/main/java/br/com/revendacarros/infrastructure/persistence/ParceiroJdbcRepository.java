package br.com.revendacarros.infrastructure.persistence;

import br.com.revendacarros.domain.model.Parceiro;
import br.com.revendacarros.domain.repository.ParceiroRepository;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ParceiroJdbcRepository implements ParceiroRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public ParceiroJdbcRepository(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public Parceiro saveParceiro(int empresaId, Parceiro parceiro) {
        namedJdbcTemplate.update("""
                INSERT INTO revenda.parceiros
                (id, empresa_id, nome, cidade, telefone, email, taxa_comissao, habilitado, criado_em, atualizado_em)
                VALUES (:id, :empresaId, :nome, :cidade, :telefone, :email, :taxaComissao, :habilitado, :criadoEm, :atualizadoEm)
                """,
            new MapSqlParameterSource()
                .addValue("id", parceiro.getId())
                .addValue("empresaId", empresaId)
                .addValue("nome", parceiro.getNome())
                .addValue("cidade", parceiro.getCidade())
                .addValue("telefone", parceiro.getTelefone())
                .addValue("email", parceiro.getEmail())
                .addValue("taxaComissao", parceiro.getTaxaComissao())
                .addValue("habilitado", parceiro.isHabilitado())
                .addValue("criadoEm", Timestamp.from(parceiro.getCriadoEm()))
                .addValue("atualizadoEm", parceiro.getAtualizadoEm() == null ? null : Timestamp.from(parceiro.getAtualizadoEm()))
        );
        return parceiro;
    }

    @Override
    public Parceiro updateParceiro(int empresaId, Parceiro parceiro) {
        namedJdbcTemplate.update("""
                UPDATE revenda.parceiros
                SET nome = :nome, cidade = :cidade, telefone = :telefone, email = :email,
                    taxa_comissao = :taxaComissao, habilitado = :habilitado, atualizado_em = :atualizadoEm
                WHERE id = :id AND empresa_id = :empresaId
                """,
            new MapSqlParameterSource()
                .addValue("nome", parceiro.getNome())
                .addValue("cidade", parceiro.getCidade())
                .addValue("telefone", parceiro.getTelefone())
                .addValue("email", parceiro.getEmail())
                .addValue("taxaComissao", parceiro.getTaxaComissao())
                .addValue("habilitado", parceiro.isHabilitado())
                .addValue("atualizadoEm", Timestamp.from(parceiro.getAtualizadoEm()))
                .addValue("id", parceiro.getId())
                .addValue("empresaId", empresaId)
        );
        return parceiro;
    }

    @Override
    public List<Parceiro> findParceirosHabilitados(int empresaId) {
        return namedJdbcTemplate.query("""
                SELECT * FROM revenda.parceiros WHERE empresa_id = :empresaId AND habilitado = TRUE ORDER BY nome ASC
                """,
            new MapSqlParameterSource("empresaId", empresaId),
            new ParceiroRowMapper());
    }

    @Override
    public void setHabilitado(int empresaId, UUID id, boolean habilitado) {
        namedJdbcTemplate.update("""
                UPDATE revenda.parceiros
                SET habilitado = :habilitado, atualizado_em = :atualizadoEm
                WHERE id = :id AND empresa_id = :empresaId
                """,
            new MapSqlParameterSource()
                .addValue("habilitado", habilitado)
                .addValue("atualizadoEm", Timestamp.from(Instant.now()))
                .addValue("id", id)
                .addValue("empresaId", empresaId));
    }

    @Override
    public Optional<Parceiro> findParceiroById(int empresaId, UUID id) {
        List<Parceiro> result = namedJdbcTemplate.query("""
                SELECT * FROM revenda.parceiros WHERE id = :id AND empresa_id = :empresaId
                """,
            new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresaId", empresaId),
            new ParceiroRowMapper());
        return result.stream().findFirst();
    }

    @Override
    public Optional<Parceiro> findParceiroByNome(int empresaId, String nome) {
        List<Parceiro> result = namedJdbcTemplate.query("""
                SELECT * FROM revenda.parceiros WHERE nome = :nome AND empresa_id = :empresaId
                """,
            new MapSqlParameterSource()
                .addValue("nome", nome)
                .addValue("empresaId", empresaId),
            new ParceiroRowMapper());
        return result.stream().findFirst();
    }

    private static class ParceiroRowMapper implements RowMapper<Parceiro> {

        @Override
        public Parceiro mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = rs.getObject("id", UUID.class);
            int empresaId = rs.getInt("empresa_id");
            String nome = rs.getString("nome");
            String cidade = rs.getString("cidade");
            String telefone = rs.getString("telefone");
            String email = rs.getString("email");
            BigDecimal taxaComissao = rs.getBigDecimal("taxa_comissao");
            Instant criadoEm = rs.getTimestamp("criado_em").toInstant();
            Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
            boolean habilitado = true;
        try {
            habilitado = rs.getBoolean("habilitado");
        } catch (SQLException ignored) {
            // column may not exist on old DB
        }
        return new Parceiro(
                id,
                empresaId,
                nome,
                cidade,
                telefone,
                email,
                taxaComissao,
                habilitado,
                criadoEm,
                atualizadoEm == null ? null : atualizadoEm.toInstant()
            );
        }
    }
}
