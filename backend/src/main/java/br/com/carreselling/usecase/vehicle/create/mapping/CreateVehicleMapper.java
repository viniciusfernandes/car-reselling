package br.com.carreselling.usecase.vehicle.create.mapping;

import br.com.carreselling.usecase.vehicle.create.contract.CreateVehicleRequest;

public class CreateVehicleMapper {

    private CreateVehicleMapper() {
    }

    public static CreateVehicleRequest normalize(CreateVehicleRequest request) {
        if (request == null || request.licensePlate() == null) {
            return request;
        }
        return new CreateVehicleRequest(
            request.licensePlate().trim().toUpperCase(),
            request.renavam(),
            request.vin(),
            request.year(),
            request.color(),
            request.model(),
            request.brand(),
            request.supplierSource(),
            request.purchasePrice(),
            request.freightCost(),
            request.purchaseCommission()
        );
    }
}
