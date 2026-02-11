package br.com.carreselling.usecase.brand.list.mapping;

import br.com.carreselling.application.service.model.BrandSummary;
import br.com.carreselling.usecase.brand.list.contract.BrandItem;

public class BrandListMapper {

    private BrandListMapper() {
    }

    public static BrandItem toItem(BrandSummary brand) {
        return new BrandItem(brand.id(), brand.name());
    }
}
