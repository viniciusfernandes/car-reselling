package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.DistributedVehiclesFilter;
import br.com.carreselling.application.service.model.DistributedVehiclesReport;

public interface IReportService {

    DistributedVehiclesReport distributedVehiclesReport(DistributedVehiclesFilter filter);
}
