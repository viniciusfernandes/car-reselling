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
    public CashBalance findCashBalance(int companyId) {
        List<CashBalance> result = jdbcTemplate.query("""
                        SELECT company_id, amount, updated_at
                        FROM cash_balance
                        WHERE company_id = ?
                        """,
                (rs, rowNum) -> new CashBalance(
                        rs.getInt("company_id"),
                        rs.getBigDecimal("amount"),
                        rs.getTimestamp("updated_at").toInstant()
                ),
                companyId
        );
        if (result.isEmpty()) {
            throw new IllegalStateException("cash_balance table has no record for company " + companyId + ". Run Liquibase migrations.");
        }
        return result.getFirst();
    }

    @Override
    public void updateCashBalance(int companyId, double amount) {
        jdbcTemplate.update("""
                        UPDATE cash_balance
                        SET amount = ?, updated_at = ?
                        WHERE company_id = ?
                        """,
                amount,
                LocalDateTime.now(),
                companyId
        );
    }

    @Override
    public void saveCashBalanceHistory(int companyId, CashBalanceHistory history) {
        jdbcTemplate.update("""
                        INSERT INTO cash_balance_history (id, company_id, amount, changed_at, changed_by)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                UuidGenerator.generate().toString(),
                companyId,
                history.amount,
                history.changedAt,
                history.changedBy
        );
    }
}
