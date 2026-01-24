package br.com.carreselling.usecase.partner.list.mapping;

import br.com.carreselling.application.service.model.PartnerSummary;
import br.com.carreselling.usecase.partner.list.contract.PartnerItem;

public class PartnerListMapper {

    private PartnerListMapper() {
    }

    public static PartnerItem toItem(PartnerSummary summary) {
        return new PartnerItem(summary.id(), summary.name(), summary.city());
    }
}
