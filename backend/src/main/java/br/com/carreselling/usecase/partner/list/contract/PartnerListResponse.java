package br.com.carreselling.usecase.partner.list.contract;

import java.util.List;

public record PartnerListResponse(List<PartnerItem> partners) {
}
