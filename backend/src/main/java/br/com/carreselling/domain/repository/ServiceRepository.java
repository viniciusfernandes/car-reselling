package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.ServiceOnVehicle;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRepository {

    ServiceOnVehicle saveService(ServiceOnVehicle serviceEntry);

    Optional<ServiceOnVehicle> findServiceById(UUID id);

    List<ServiceOnVehicle> findServiceByVehicleId(UUID vehicleId);

    ServiceOnVehicle updateService(ServiceOnVehicle serviceEntry);

    void deleteService(UUID id);

    BigDecimal findServiceTotalByVehicleId(UUID vehicleId);

    void  deleteServicesByVehicleId(UUID vehicleId);

    boolean existsOpenServiceByVehicleId(UUID vehicleId);
}
