package br.com.carreselling.usecase.partner.update.contract;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record UpdatePartnerRequest(
    @NotBlank String name,
    String city,
    String phone,
    String email,
    @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal commissionRate
) {
}
