package br.com.carreselling.application.service.model;

import java.util.UUID;

import java.math.BigDecimal;

public record PartnerSummary(UUID id, String name, String city, String phone, String email, BigDecimal commissionRate) {
}
