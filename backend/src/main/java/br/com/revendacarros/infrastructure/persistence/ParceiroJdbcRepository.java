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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ParceiroJdbcRepository implements ParceiroRepository {

    private final JdbcTemplate jdbcTemplate;

    public ParceiroJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Parceiro saveParceiro(int empresaId, Parceiro parceiro) {
        jdbcTemplate.update("""
                INSERT INTO parceiros
                (id, empresa_id, nome, cidade, telefone, email, taxa_comissao, habilitado, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
            parceiro.getId(),
            empresaId,
            parceiro.getNome(),
            parceiro.getCidade(),
            parceiro.getTelefone(),
            parceiro.getEmail(),
            parceiro.getTaxaComissao(),
            parceiro.isHabilitado(),
            Timestamp.from(parceiro.getCriadoEm()),
            parceiro.getAtualizadoEm() == null ? null : Timestamp.from(parceiro.getAtualizadoEm())
        );
        return parceiro;
    }

    @Override
    public Parceiro updateParceiro(int empresaId, Parceiro parceiro) {
        jdbcTemplate.update("""
                UPDATE parceiros
                SET nome = ?, cidade = ?, telefone = ?, email = ?, taxa_comissao = ?, habilitado = ?, atualizado_em = ?
                WHERE id = ? AND empresa_id = ?
                """,
            parceiro.getNome(),
            parceiro.getCidade(),
            parceiro.getTelefone(),
            parceiro.getEmail(),
            parceiro.getTaxaComissao(),
            parceiro.isHabilitado(),
            Timestamp.from(parceiro.getAtualizadoEm()),
            parceiro.getId(),
            empresaId
        );
        return parceiro;
    }

    @Override
    public List<Parceiro> findParceirosHabilitados(int empresaId) {
        return jdbcTemplate.query("""
                SELECT * FROM parceiros WHERE empresa_id = ? AND habilitado = TRUE ORDER BY nome ASC
                """,
            new ParceiroRowMapper(),
            empresaId);
    }

    @Override
    public void setHabilitado(int empresaId, UUID id, boolean habilitado) {
        jdbcTemplate.update("UPDATE parceiros SET habilitado = ?, atualizado_em = ? WHERE id = ? AND empresa_id = ?",
            habilitado, Timestamp.from(Instant.now()), id, empresaId);
    }

    @Override
    public Optional<Parceiro> findParceiroById(int empresaId, UUID id) {
        List<Parceiro> result = jdbcTemplate.query("""
                SELECT * FROM parceiros WHERE id = ? AND empresa_id = ?
                """,
            new ParceiroRowMapper(),
            id,
            empresaId);
        return result.stream().findFirst();
    }

    @Override
    public Optional<Parceiro> findParceiroByNome(int empresaId, String nome) {
        List<Parceiro> result = jdbcTemplate.query("""
                SELECT * FROM parceiros WHERE nome = ? AND empresa_id = ?
                """,
            new ParceiroRowMapper(),
            nome,
            empresaId);
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
