package br.com.carreselling.infrastructure.persistence;

import br.com.carreselling.common.UuidGenerator;
import br.com.carreselling.domain.model.CashBalance;
import br.com.carreselling.domain.model.CashBalanceHistory;
import br.com.carreselling.domain.repository.CashBalanceRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CashBalanceJdbcRepository implements CashBalanceRepository {

    private final JdbcTemplate jdbcTemplate;

    public CashBalanceJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CashBalance findCashBalance() {
        List<CashBalance> result = jdbcTemplate.query("""
                        SELECT amount, updated_at
                        FROM cash_balance
                        """,
                (rs, rowNum) -> new CashBalance(
                        rs.getBigDecimal("amount"),
                        rs.getTimestamp("updated_at").toInstant()
                )
        );
        if (result.isEmpty()) {
            throw new IllegalStateException("cash_balance table has no record. Run Liquibase migrations.");
        }
        return result.getFirst();
    }

    @Override
    public void updateCashBalance(double amount) {
        jdbcTemplate.update("""
                        UPDATE cash_balance
                        SET amount = ?, updated_at = ?
                        """,
                amount,
                LocalDateTime.now()
        );
    }

    @Override
    public void saveCashBalanceHistory(CashBalanceHistory history) {
        jdbcTemplate.update("""
                        INSERT INTO cash_balance_history (id, amount, changed_at, changed_by)
                        VALUES (?, ?, ?, ?)
                        """,
                UuidGenerator.generate().toString(),
                history.amount,
                history.changedAt,
                history.changedBy
        );
    }
}
