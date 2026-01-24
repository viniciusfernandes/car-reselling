package br.com.carreselling.usecase.partner.create.contract;

import jakarta.validation.constraints.NotBlank;

public record CreatePartnerRequest(@NotBlank String name, String city) {
}
