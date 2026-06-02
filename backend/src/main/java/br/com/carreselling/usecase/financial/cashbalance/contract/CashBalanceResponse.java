package br.com.carreselling.usecase.financial.cashbalance.contract;

import java.math.BigDecimal;
import java.time.Instant;

public record CashBalanceResponse(BigDecimal amount, Instant updatedAt) {
}
