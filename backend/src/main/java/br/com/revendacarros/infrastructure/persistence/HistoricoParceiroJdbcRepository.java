package br.com.revendacarros.infrastructure.persistence;

import br.com.revendacarros.domain.model.HistoricoParceiro;
import br.com.revendacarros.domain.repository.HistoricoParceiroRepository;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class HistoricoParceiroJdbcRepository implements HistoricoParceiroRepository {

    private final JdbcTemplate jdbcTemplate;

    public HistoricoParceiroJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveHistorico(int empresaId, HistoricoParceiro historico) {
        jdbcTemplate.update("""
                INSERT INTO historico_parceiro
                (id, empresa_id, parceiro_id, nome, cidade, telefone, email, taxa_comissao, alterado_em, alterado_por)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
            historico.getId(),
            empresaId,
            historico.getParceiroId(),
            historico.getNome(),
            historico.getCidade(),
            historico.getTelefone(),
            historico.getEmail(),
            historico.getTaxaComissao(),
            Timestamp.from(historico.getAlteradoEm()),
            historico.getAlteradoPor()
        );
    }

    @Override
    public List<HistoricoParceiro> findHistoricoByParceiroId(int empresaId, UUID parceiroId) {
        return jdbcTemplate.query("""
                SELECT * FROM historico_parceiro
                WHERE parceiro_id = ? AND empresa_id = ?
                ORDER BY alterado_em DESC
                """,
            new HistoricoParceiroRowMapper(),
            parceiroId,
            empresaId
        );
    }

    private static class HistoricoParceiroRowMapper implements RowMapper<HistoricoParceiro> {

        @Override
        public HistoricoParceiro mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = rs.getObject("id", UUID.class);
            int empresaId = rs.getInt("empresa_id");
            UUID parceiroId = rs.getObject("parceiro_id", UUID.class);
            String nome = rs.getString("nome");
            String cidade = rs.getString("cidade");
            String telefone = rs.getString("telefone");
            String email = rs.getString("email");
            BigDecimal taxaComissao = rs.getBigDecimal("taxa_comissao");
            Instant alteradoEm = rs.getTimestamp("alterado_em").toInstant();
            String alteradoPor = rs.getString("alterado_por");
            return new HistoricoParceiro(id, empresaId, parceiroId, nome, cidade, telefone, email,
                taxaComissao, alteradoEm, alteradoPor);
        }
    }
}
