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

    UUID createVehicle(int companyId,
                       String licensePlate,
                       String renavam,
                       String vin,
                       int year,
                       String color,
                       String model,
                       String brand,
                       SupplierSource supplierSource,
                       BigDecimal purchasePrice,
                       BigDecimal freightCost,
                       BigDecimal purchaseCommission,
                       BigDecimal valorFipe);

    VehicleDetail getVehicle(int companyId, UUID vehicleId);

    List<VehicleSummary> listVehicles(int companyId, VehicleStatus status, String query, Boolean onService, int page, int size);

    long countVehicles(int companyId, VehicleStatus status, String query, Boolean onService);

    void updateVehicle(int companyId,
                       UUID vehicleId,
                       int year,
                       String color,
                       String model,
                       String brand,
                       SupplierSource supplierSource,
                       BigDecimal purchasePrice,
                       BigDecimal freightCost,
                       BigDecimal purchaseCommission,
                       UUID invoiceDocumentId,
                       UUID paymentReceiptDocumentId,
                       BigDecimal valorFipe);

    void updateSellingPrice(int companyId, UUID vehicleId, BigDecimal sellingPrice);

    VehicleTaxes getVehicleTaxes(int companyId, UUID vehicleId);

    void transitionStatus(int companyId, UUID vehicleId, VehicleStatus targetStatus, UUID assignedPartnerId);

    void assignPartner(int companyId, UUID vehicleId, UUID partnerId);

    void deleteVehicle(int companyId, UUID vehicleId);
}
