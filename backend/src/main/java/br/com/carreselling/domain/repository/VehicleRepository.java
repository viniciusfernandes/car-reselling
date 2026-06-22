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

    Vehicle saveVehicle(int companyId, Vehicle vehicle);

    Optional<Vehicle> findVehicleById(int companyId, UUID id);

    Optional<Vehicle> findVehicleByLicensePlate(int companyId, String licensePlate);

    Optional<Vehicle> findVehicleByRenavam(int companyId, String renavam);

    Optional<Vehicle> findVehicleByVin(int companyId, String vin);

    List<Vehicle> findVehicleByFilter(int companyId, VehicleStatus status, String query, Boolean onService, int offset, int size);

    long countVehicleByFilter(int companyId, VehicleStatus status, String query, Boolean onService);

    Vehicle updateVehicle(int companyId, Vehicle vehicle);

    void deleteVehicle(int companyId, UUID id);

    BigDecimal findVehicleServicesTotalByVehicleId(int companyId, UUID vehicleId);

    int countVehicleDocumentsByVehicleId(int companyId, UUID vehicleId);

    List<DistribuitedVehicle> distributedVehiclesReport(int companyId, DistributedVehiclesFilter filter);

    List<SoldVehicle> findTotalServicesFromSoldVehicles(int companyId, DistributedVehiclesFilter filter);

    VehiclesTotalCost findVehicleTotalCost(int companyId);
}
