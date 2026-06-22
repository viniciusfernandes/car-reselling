package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.DistributedVehiclesFilter;
import br.com.carreselling.application.service.model.DistributedVehiclesReport;
import br.com.carreselling.application.service.model.SoldVehiclesReport;

public interface IReportService {

    DistributedVehiclesReport distributedVehiclesReport(int companyId, DistributedVehiclesFilter filter);

    SoldVehiclesReport soldVehiclesReport(int companyId, DistributedVehiclesFilter filter);
}
