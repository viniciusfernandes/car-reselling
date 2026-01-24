package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.ServiceEntry;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRepository {

    ServiceEntry saveService(ServiceEntry serviceEntry);

    Optional<ServiceEntry> findServiceById(UUID id);

    List<ServiceEntry> findServiceByVehicleId(UUID vehicleId);

    ServiceEntry updateService(ServiceEntry serviceEntry);

    void deleteService(UUID id);

    BigDecimal findServiceTotalByVehicleId(UUID vehicleId);
}
