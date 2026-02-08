package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.VehicleDetail;
import br.com.carreselling.application.service.model.VehicleSummary;
import br.com.carreselling.application.service.model.VehicleTaxes;
import br.com.carreselling.domain.model.SupplierSource;
import br.com.carreselling.domain.model.VehicleStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IVehicleService {

    UUID createVehicle(String licensePlate,
                       String renavam,
                       String vin,
                       int year,
                       String color,
                       String model,
                       String brand,
                       SupplierSource supplierSource,
                       BigDecimal purchasePrice,
                       BigDecimal freightCost,
                       BigDecimal purchaseCommission);

    VehicleDetail getVehicle(UUID vehicleId);

    List<VehicleSummary> listVehicles(VehicleStatus status, String query, int page, int size);

    long countVehicles(VehicleStatus status, String query);

    void updateVehicle(UUID vehicleId,
                       int year,
                       String color,
                       String model,
                       String brand,
                       SupplierSource supplierSource,
                       BigDecimal purchasePrice,
                       BigDecimal freightCost,
                       BigDecimal purchaseCommission,
                       UUID invoiceDocumentId,
                       UUID paymentReceiptDocumentId);

    void updateSellingPrice(UUID vehicleId, BigDecimal sellingPrice);

    VehicleTaxes getVehicleTaxes(UUID vehicleId);

    void transitionStatus(UUID vehicleId, VehicleStatus targetStatus, UUID assignedPartnerId);

    void assignPartner(UUID vehicleId, UUID partnerId);
}
