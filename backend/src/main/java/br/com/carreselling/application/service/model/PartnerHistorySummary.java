package br.com.carreselling.application.service.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PartnerHistorySummary(
    UUID id,
    UUID partnerId,
    String name,
    String city,
    String phone,
    String email,
    BigDecimal commissionRate,
    Instant changedAt,
    String changedBy
) {
}
