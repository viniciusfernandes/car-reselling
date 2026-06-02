package br.com.carreselling.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public class CashBalance {

    public BigDecimal amount;
    public Instant updatedAt;

    public CashBalance(  BigDecimal amount, Instant updatedAt) {
        this.amount = amount;
        this.updatedAt = updatedAt;
    }
}
