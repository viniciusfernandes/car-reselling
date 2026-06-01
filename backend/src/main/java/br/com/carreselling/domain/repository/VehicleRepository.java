package br.com.carreselling.domain.repository;

import br.com.carreselling.application.service.model.VehiclesTotalCost;
import br.com.carreselling.application.service.model.DistributedVehiclesFilter;
import br.com.carreselling.application.service.model.DistribuitedVehicle;
import br.com.carreselling.application.service.model.SoldVehicle;
import br.com.carreselling.domain.model.Vehicle;
import br.com.carreselling.domain.model.VehicleStatus;

import java.math.BigDecimal;
import java.util.*;

public interface VehicleRepository {

    Vehicle saveVehicle(Vehicle vehicle);

    Optional<Vehicle> findVehicleById(UUID id);

    Optional<Vehicle> findVehicleByLicensePlate(String licensePlate);

    Optional<Vehicle> findVehicleByRenavam(String renavam);

    Optional<Vehicle> findVehicleByVin(String vin);

    List<Vehicle> findVehicleByFilter(VehicleStatus status, String query, Boolean onService, int offset, int size);

    long countVehicleByFilter(VehicleStatus status, String query, Boolean onService);

    Vehicle updateVehicle(Vehicle vehicle);

    void deleteVehicle(UUID id);

    BigDecimal findVehicleServicesTotalByVehicleId(UUID vehicleId);

    int countVehicleDocumentsByVehicleId(UUID vehicleId);

    List<DistribuitedVehicle> distributedVehiclesReport(DistributedVehiclesFilter filter);

    List<SoldVehicle> findTotalServicesFromSoldVehicles(DistributedVehiclesFilter filter);

    VehiclesTotalCost findVehicleTotalCost();
}
