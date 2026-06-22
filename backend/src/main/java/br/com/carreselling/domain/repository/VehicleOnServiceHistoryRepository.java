package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.VehicleOnServiceHistory;

import java.util.List;
import java.util.UUID;

public interface VehicleOnServiceHistoryRepository {

    void save(int companyId, VehicleOnServiceHistory history);

    List<VehicleOnServiceHistory> findByVehicleId(int companyId, UUID vehicleId);

    void deleteByVehicleId(int companyId, UUID vehicleId);
}
