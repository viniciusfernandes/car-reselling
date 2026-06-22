package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.ServiceOnVehicle;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRepository {

    ServiceOnVehicle saveService(int companyId, ServiceOnVehicle serviceEntry);

    Optional<ServiceOnVehicle> findServiceById(int companyId, UUID id);

    List<ServiceOnVehicle> findServiceByVehicleId(int companyId, UUID vehicleId);

    ServiceOnVehicle updateService(int companyId, ServiceOnVehicle serviceEntry);

    void deleteService(int companyId, UUID id);

    BigDecimal findServiceTotalByVehicleId(int companyId, UUID vehicleId);

    void deleteServicesByVehicleId(int companyId, UUID vehicleId);

    boolean existsOpenServiceByVehicleId(int companyId, UUID vehicleId);
}
