package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.Vehicle;
import br.com.carreselling.domain.model.VehicleStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository {

    Vehicle saveVehicle(Vehicle vehicle);

    Optional<Vehicle> findVehicleById(UUID id);

    Optional<Vehicle> findVehicleByLicensePlate(String licensePlate);

    Optional<Vehicle> findVehicleByRenavam(String renavam);

    Optional<Vehicle> findVehicleByVin(String vin);

    List<Vehicle> findVehicleByFilter(VehicleStatus status, String query, int offset, int size);

    long countVehicleByFilter(VehicleStatus status, String query);

    Vehicle updateVehicle(Vehicle vehicle);

    void deleteVehicle(UUID id);

    BigDecimal findVehicleServicesTotalByVehicleId(UUID vehicleId);

    int countVehicleDocumentsByVehicleId(UUID vehicleId);
}
