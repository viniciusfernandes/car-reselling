package br.com.carreselling.usecase.partner.list.contract;

import java.math.BigDecimal;
import java.util.UUID;

public record PartnerItem(UUID id, String name, String city, String phone, String email, BigDecimal commissionRate) {
}
