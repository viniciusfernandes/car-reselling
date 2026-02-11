package br.com.carreselling.usecase.brand.model.list.mapping;

import br.com.carreselling.application.service.model.VehicleModelSummary;
import br.com.carreselling.usecase.brand.model.list.contract.VehicleModelItem;

public class VehicleModelListMapper {

    private VehicleModelListMapper() {
    }

    public static VehicleModelItem toItem(VehicleModelSummary model) {
        return new VehicleModelItem(model.id(), model.name());
    }
}
