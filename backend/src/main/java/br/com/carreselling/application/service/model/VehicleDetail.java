package br.com.carreselling.application.service.model;

import br.com.carreselling.domain.model.SupplierSource;
import br.com.carreselling.domain.model.VehicleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record VehicleDetail(UUID id,
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
                            UUID purchaseInvoiceDocumentId,
                            UUID purchasePaymentReceiptDocumentId,
                            VehicleStatus status,
                            UUID assignedPartnerId,
                            String assignedPartnerName,
                            BigDecimal servicesTotal,
                            BigDecimal totalCost,
                            int documentsCount,
                            Instant createdAt,
                            Instant updatedAt) {
}
