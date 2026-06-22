package br.com.carreselling.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class CashBalanceHistory {

    public UUID id;
    public int companyId;
    public double amount;
    public LocalDateTime changedAt;
    public String changedBy;

    public CashBalanceHistory(int companyId, double amount, String changedBy) {
        this.companyId = companyId;
        this.amount = amount;
        this.changedAt = LocalDateTime.now();
        this.changedBy = changedBy;
    }

    public int getCompanyId() {
        return companyId;
    }
}
