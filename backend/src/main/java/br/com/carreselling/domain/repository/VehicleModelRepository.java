package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.VehicleModel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleModelRepository {

    VehicleModel saveModel(VehicleModel model);

    List<VehicleModel> findModelsByBrandId(UUID brandId);

    Optional<VehicleModel> findModelById(UUID id);

    Optional<VehicleModel> findModelByBrandIdAndName(UUID brandId, String name);
}
