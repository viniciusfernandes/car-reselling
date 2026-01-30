package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.DistributedVehiclesFilter;
import br.com.carreselling.application.service.model.DistributedVehiclesReport;
import br.com.carreselling.application.service.model.SoldVehiclesReport;

public interface IReportService {

    DistributedVehiclesReport distributedVehiclesReport(DistributedVehiclesFilter filter);

    SoldVehiclesReport soldVehiclesReport(DistributedVehiclesFilter filter);
}
