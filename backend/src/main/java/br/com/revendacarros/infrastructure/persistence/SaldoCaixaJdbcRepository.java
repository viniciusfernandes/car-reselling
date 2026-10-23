package br.com.revendacarros.infrastructure.persistence;

import br.com.revendacarros.domain.model.SaldoCaixa;
import br.com.revendacarros.domain.model.HistoricoSaldoCaixa;
import br.com.revendacarros.domain.repository.SaldoCaixaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class SaldoCaixaJdbcRepository implements SaldoCaixaRepository {

    private final JdbcTemplate jdbcTemplate;

    public SaldoCaixaJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public SaldoCaixa findSaldoCaixa(int empresaId) {
        List<SaldoCaixa> result = jdbcTemplate.query("""
                        SELECT empresa_id, valor, atualizado_em
                        FROM saldo_caixa
                        WHERE empresa_id = ?
                        """,
                (rs, rowNum) -> new SaldoCaixa(
                        rs.getInt("empresa_id"),
                        rs.getBigDecimal("valor"),
                        rs.getTimestamp("atualizado_em").toInstant()
                ),
                empresaId
        );
        if (result.isEmpty()) {
            throw new IllegalStateException("A tabela saldo_caixa não possui registro para a empresa " + empresaId + ". Execute as migrações do Liquibase.");
        }
        return result.getFirst();
    }

    @Override
    public void updateSaldoCaixa(int empresaId, double valor) {
        jdbcTemplate.update("""
                        UPDATE saldo_caixa
                        SET valor = ?, atualizado_em = ?
                        WHERE empresa_id = ?
                        """,
                valor,
                LocalDateTime.now(),
                empresaId
        );
    }

    @Override
    public void saveHistoricoSaldoCaixa(int empresaId, HistoricoSaldoCaixa historico) {
        jdbcTemplate.update("""
                        INSERT INTO historico_saldo_caixa (id, empresa_id, valor, alterado_em, alterado_por)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                historico.id,
                empresaId,
                historico.valor,
                historico.alteradoEm,
                historico.alteradoPor
        );
    }
}
