package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.VehicleModelSummary;
import br.com.carreselling.domain.repository.VehicleModelRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class VehicleModelService implements IVehicleModelService {

    private final VehicleModelRepository vehicleModelRepository;

    public VehicleModelService(VehicleModelRepository vehicleModelRepository) {
        this.vehicleModelRepository = vehicleModelRepository;
    }

    @Override
    public List<VehicleModelSummary> listModelsByBrandId(UUID brandId) {
        return vehicleModelRepository.findModelsByBrandId(brandId)
            .stream()
            .map(model -> new VehicleModelSummary(model.getId(), model.getName()))
            .toList();
    }
}
