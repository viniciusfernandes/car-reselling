package br.com.carreselling.usecase.vehicle.taxes.mapping;

import br.com.carreselling.application.service.model.VehicleTaxes;
import br.com.carreselling.usecase.vehicle.taxes.contract.VehicleTaxesResponse;

public class VehicleTaxesMapper {

    private VehicleTaxesMapper() {
    }

    public static VehicleTaxesResponse toResponse(VehicleTaxes taxes) {
        return new VehicleTaxesResponse(
            taxes.icms(),
            taxes.pis(),
            taxes.cofins(),
            taxes.csll(),
            taxes.irpj(),
            taxes.totalTaxes()
        );
    }
}
