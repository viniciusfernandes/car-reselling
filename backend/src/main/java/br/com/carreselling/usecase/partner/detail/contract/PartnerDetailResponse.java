package br.com.carreselling.usecase.partner.detail.contract;

import java.math.BigDecimal;
import java.util.UUID;

public record PartnerDetailResponse(
    UUID id,
    String name,
    String city,
    String phone,
    String email,
    BigDecimal commissionRate
) {
}
