package br.com.carreselling.usecase.vehicle.detail.mapping;

import br.com.carreselling.application.service.model.VehicleDetail;
import br.com.carreselling.usecase.vehicle.detail.contract.VehicleDetailResponse;

public class VehicleDetailMapper {

    private VehicleDetailMapper() {
    }

    public static VehicleDetailResponse toResponse(VehicleDetail detail) {
        return new VehicleDetailResponse(
            detail.id(),
            detail.licensePlate(),
            detail.renavam(),
            detail.vin(),
            detail.year(),
            detail.color(),
            detail.model(),
            detail.brand(),
            detail.supplierSource(),
            detail.purchasePrice(),
            detail.freightCost(),
            detail.sellingPrice(),
            detail.purchaseInvoiceDocumentId(),
            detail.purchasePaymentReceiptDocumentId(),
            detail.status(),
            detail.assignedPartnerId(),
            detail.assignedPartnerName(),
            detail.servicesTotal(),
            detail.totalCost(),
            detail.documentsCount(),
            detail.createdAt(),
            detail.updatedAt()
        );
    }
}
