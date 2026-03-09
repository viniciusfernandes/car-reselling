package br.com.carreselling.usecase.partner.create.contract;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CreatePartnerRequest(
    @NotBlank String name,
    String city,
    String phone,
    String email,
    BigDecimal commissionRate
) {
}
