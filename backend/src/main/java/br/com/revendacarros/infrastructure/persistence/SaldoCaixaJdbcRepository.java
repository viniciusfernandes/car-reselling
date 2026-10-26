package br.com.revendacarros.infrastructure.persistence;

import br.com.revendacarros.domain.model.SaldoCaixa;
import br.com.revendacarros.domain.model.HistoricoSaldoCaixa;
import br.com.revendacarros.domain.repository.SaldoCaixaRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class SaldoCaixaJdbcRepository implements SaldoCaixaRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public SaldoCaixaJdbcRepository(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public SaldoCaixa findSaldoCaixa(int empresaId) {
        List<SaldoCaixa> result = namedJdbcTemplate.query("""
                        SELECT empresa_id, valor, atualizado_em
                        FROM revenda.saldo_caixa
                        WHERE empresa_id = :empresaId
                        """,
                new MapSqlParameterSource("empresaId", empresaId),
                (rs, rowNum) -> new SaldoCaixa(
                        rs.getInt("empresa_id"),
                        rs.getBigDecimal("valor"),
                        rs.getTimestamp("atualizado_em").toInstant()
                )
        );
        if (result.isEmpty()) {
            return null;
        }
        return result.getFirst();
    }

    @Override
    public boolean existsSaldoCaixa(int empresaId) {
        return Boolean.TRUE.equals(namedJdbcTemplate.queryForObject("""
                        SELECT exists (select 1  FROM revenda.saldo_caixa WHERE empresa_id = :empresaId)
                        """,
                new MapSqlParameterSource("empresaId", empresaId),
                Boolean.class
        ));
    }

    @Override
    public void createSaldoCaixa(int empresaId, double valor) {
        namedJdbcTemplate.update("""
                        INSERT INTO revenda.saldo_caixa ( empresa_id, valor, atualizado_em)
                        VALUES (:empresaId, :valor, :atualizadoEm)                        
                        """,
                new MapSqlParameterSource()
                        .addValue("valor", valor)
                        .addValue("atualizadoEm", LocalDateTime.now())
                        .addValue("empresaId", empresaId)
        );
    }

    @Override
    public void updateSaldoCaixa(int empresaId, double valor) {
        namedJdbcTemplate.update("""
                        UPDATE revenda.saldo_caixa
                        SET valor = :valor, atualizado_em = :atualizadoEm
                        WHERE empresa_id = :empresaId
                        """,
                new MapSqlParameterSource()
                        .addValue("valor", valor)
                        .addValue("atualizadoEm", LocalDateTime.now())
                        .addValue("empresaId", empresaId)
        );
    }

    @Override
    public void saveHistoricoSaldoCaixa(int empresaId, HistoricoSaldoCaixa historico) {
        namedJdbcTemplate.update("""
                        INSERT INTO revenda.historico_saldo_caixa (id, empresa_id, valor, alterado_em, alterado_por)
                        VALUES (:id, :empresaId, :valor, :alteradoEm, :alteradoPor)
                        """,
                new MapSqlParameterSource()
                        .addValue("id", historico.id)
                        .addValue("empresaId", empresaId)
                        .addValue("valor", historico.valor)
                        .addValue("alteradoEm", historico.alteradoEm)
                        .addValue("alteradoPor", historico.alteradoPor)
        );
    }
}
