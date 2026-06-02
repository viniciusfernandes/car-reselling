package br.com.carreselling.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class CashBalanceHistory {

    public UUID id;
    public double amount;
    public LocalDateTime changedAt;
    public String changedBy;

    public CashBalanceHistory(double amount, String changedBy) {
        this.amount = amount;
        this.changedAt = LocalDateTime.now();
        this.changedBy = changedBy;
    }
}
