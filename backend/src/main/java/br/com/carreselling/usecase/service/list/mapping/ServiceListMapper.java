package br.com.carreselling.usecase.service.list.mapping;

import br.com.carreselling.application.service.model.ServiceSummary;
import br.com.carreselling.usecase.service.list.contract.ServiceItem;

public class ServiceListMapper {

    private ServiceListMapper() {
    }

    public static ServiceItem toItem(ServiceSummary summary) {
        return new ServiceItem(
            summary.id(),
            summary.vehicleId(),
            summary.serviceType(),
            summary.description(),
            summary.serviceValue(),
            summary.performedAt()
        );
    }
}
