package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.VehicleModelSummary;
import java.util.List;
import java.util.UUID;

public interface IVehicleModelService {

    List<VehicleModelSummary> listModelsByBrandId(UUID brandId);
}
