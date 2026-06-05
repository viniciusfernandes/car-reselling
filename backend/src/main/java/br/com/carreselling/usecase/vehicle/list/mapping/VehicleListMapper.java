package br.com.carreselling.usecase.vehicle.list.mapping;

import br.com.carreselling.application.service.model.VehicleSummary;
import br.com.carreselling.usecase.vehicle.list.contract.VehicleListItem;

public class VehicleListMapper {

    private VehicleListMapper() {
    }

    public static VehicleListItem toItem(VehicleSummary summary) {
        return new VehicleListItem(
            summary.id(),
            summary.licensePlate(),
            summary.brand(),
            summary.model(),
            summary.year(),
            summary.status(),
            summary.onService(),
            summary.sellingPrice(),
            summary.totalCost(),
            summary.profitMargin(),
            summary.purchaseTimeDays(),
            summary.servicesTotal(),
            summary.daysOnService()
        );
    }
}
