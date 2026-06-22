package br.com.carreselling.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public class CashBalance {

    public int companyId;
    public BigDecimal amount;
    public Instant updatedAt;

    public CashBalance(int companyId, BigDecimal amount, Instant updatedAt) {
        this.companyId = companyId;
        this.amount = amount;
        this.updatedAt = updatedAt;
    }

    public int getCompanyId() {
        return companyId;
    }
}
