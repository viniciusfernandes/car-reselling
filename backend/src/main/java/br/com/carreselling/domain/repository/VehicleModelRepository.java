package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.VehicleModel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleModelRepository {

    VehicleModel saveModel(int companyId, VehicleModel model);

    List<VehicleModel> findModelsByBrandId(int companyId, UUID brandId);

    Optional<VehicleModel> findModelById(int companyId, UUID id);

    Optional<VehicleModel> findModelByBrandIdAndName(int companyId, UUID brandId, String name);
}
