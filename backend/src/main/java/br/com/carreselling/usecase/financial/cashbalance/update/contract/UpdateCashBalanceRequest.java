package br.com.carreselling.usecase.financial.cashbalance.update.contract;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateCashBalanceRequest(
        @NotNull
        @DecimalMin(value = "0", message = "Amount must be zero or greater")
        Double amount
) {
}
