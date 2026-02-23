package br.com.carreselling.usecase.partner.history.contract;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PartnerHistoryItem(
    UUID id,
    String name,
    String city,
    String phone,
    String email,
    BigDecimal commissionRate,
    Instant changedAt,
    String changedBy
) {
}
