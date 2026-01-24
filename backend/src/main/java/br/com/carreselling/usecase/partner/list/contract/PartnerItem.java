package br.com.carreselling.usecase.partner.list.contract;

import java.util.UUID;

public record PartnerItem(UUID id, String name, String city) {
}
