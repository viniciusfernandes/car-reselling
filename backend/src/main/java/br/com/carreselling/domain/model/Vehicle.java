package br.com.carreselling.domain.model;

import br.com.carreselling.domain.exception.InvalidStateException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

public class Vehicle {

    private final UUID id;
    private final String licensePlate;
    private String renavam;
    private String vin;
    private int year;
    private String color;
    private String model;
    private String brand;
    private UUID brandId;
    private UUID modelId;
    private SupplierSource supplierSource;
    private BigDecimal purchasePrice;
    private BigDecimal freightCost;
    private BigDecimal purchaseCommission;
    private BigDecimal sellingPrice;
    private UUID purchasePaymentReceiptDocumentId;
    private UUID purchaseInvoiceDocumentId;
    private VehicleStatus status;
    private UUID assignedPartnerId;
    private Instant distributedAt;
    private LocalDate soldAt;
    private BigDecimal saleCommissionRate;
    private BigDecimal valorFipe;
    private Instant createdAt;
    private Instant updatedAt;

    public Vehicle(UUID id,
                   String licensePlate,
                   String renavam,
                   String vin,
                   int year,
                   String color,
                   String model,
                   String brand,
                   UUID brandId,
                   UUID modelId,
                   SupplierSource supplierSource,
                   BigDecimal purchasePrice,
                   BigDecimal freightCost,
                   BigDecimal purchaseCommission,
                   BigDecimal sellingPrice,
                   BigDecimal valorFipe,
                   UUID purchasePaymentReceiptDocumentId,
                   UUID purchaseInvoiceDocumentId,
                   VehicleStatus status,
                   UUID assignedPartnerId,
                   Instant distributedAt,
                   LocalDate soldAt,
                   BigDecimal saleCommissionRate,
                   Instant createdAt,
                   Instant updatedAt) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.renavam = renavam;
        this.vin = vin;
        this.year = year;
        this.color = color;
        this.model = model;
        this.brand = brand;
        this.brandId = brandId;
        this.modelId = modelId;
        this.supplierSource = supplierSource;
        this.purchasePrice = purchasePrice;
        this.freightCost = freightCost;
        this.purchaseCommission = purchaseCommission;
        this.sellingPrice = sellingPrice;
        this.valorFipe = valorFipe;
        this.purchasePaymentReceiptDocumentId = purchasePaymentReceiptDocumentId;
        this.purchaseInvoiceDocumentId = purchaseInvoiceDocumentId;
        this.status = status;
        this.assignedPartnerId = assignedPartnerId;
        this.distributedAt = distributedAt;
        this.soldAt = soldAt;
        this.saleCommissionRate = saleCommissionRate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateDetails(int year,
                              String color,
                              String model,
                              String brand,
                              SupplierSource supplierSource,
                              BigDecimal purchasePrice,
                              BigDecimal freightCost,
                              BigDecimal purchaseCommission) {
        this.year = year;
        this.color = color;
        this.model = model;
        this.brand = brand;
        this.supplierSource = supplierSource;
        this.purchasePrice = purchasePrice;
        this.freightCost = freightCost;
        this.purchaseCommission = purchaseCommission;
    }

    public void updateSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public void updateLinkedDocuments(UUID invoiceDocumentId, UUID paymentReceiptDocumentId) {
        this.purchaseInvoiceDocumentId = invoiceDocumentId;
        this.purchasePaymentReceiptDocumentId = paymentReceiptDocumentId;
    }

    public void transitionStatus(VehicleStatus targetStatus) {
        transitionStatus(targetStatus, null);
    }

    public void transitionStatus(VehicleStatus targetStatus, UUID partnerId) {
        if (targetStatus == VehicleStatus.DISTRIBUTED && partnerId == null) {
            throw new InvalidStateException("Assigned partner is required when distributing a vehicle.");
        }
        if (targetStatus == VehicleStatus.DISTRIBUTED || targetStatus == VehicleStatus.SOLD) {
            if (partnerId != null) {
                this.assignedPartnerId = partnerId;
            }
        } else {
            this.assignedPartnerId = null;
        }
        this.status = targetStatus;
    }

    public void assignPartner(UUID partnerId) {
        if (status != VehicleStatus.READY_FOR_DISTRIBUTION) {
            throw new InvalidStateException("Vehicle must be ready for distribution.");
        }
        this.assignedPartnerId = partnerId;
        this.status = VehicleStatus.DISTRIBUTED;
    }

    public void ensureServicesEditable() {
        if (status == VehicleStatus.SOLD) {
            throw new InvalidStateException("Services are read-only after sold.");
        }
    }

    public void ensureDistributionInvariant() {
        if (status == VehicleStatus.DISTRIBUTED && assignedPartnerId == null) {
            throw new InvalidStateException("Assigned partner is required when vehicle is distributed.");
        }
        if (status != VehicleStatus.DISTRIBUTED && status != VehicleStatus.SOLD && assignedPartnerId != null) {
            throw new InvalidStateException("Assigned partner can only be set when vehicle is distributed.");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getRenavam() {
        return renavam;
    }

    public String getVin() {
        return vin;
    }

    public int getYear() {
        return year;
    }

    public String getColor() {
        return color;
    }

    public String getModel() {
        return model;
    }

    public String getBrand() {
        return brand;
    }

    public UUID getBrandId() {
        return brandId;
    }

    public UUID getModelId() {
        return modelId;
    }

    public SupplierSource getSupplierSource() {
        return supplierSource;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public BigDecimal getFreightCost() {
        return freightCost;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public BigDecimal getPurchaseCommission() {
        return purchaseCommission;
    }

    public UUID getPurchasePaymentReceiptDocumentId() {
        return purchasePaymentReceiptDocumentId;
    }

    public UUID getPurchaseInvoiceDocumentId() {
        return purchaseInvoiceDocumentId;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public UUID getAssignedPartnerId() {
        return assignedPartnerId;
    }

    public Instant getDistributedAt() {
        return distributedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setRenavam(String renavam) {
        this.renavam = renavam;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public void setBrandId(UUID brandId) {
        this.brandId = brandId;
    }

    public void setModelId(UUID modelId) {
        this.modelId = modelId;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setAssignedPartnerId(UUID assignedPartnerId) {
        this.assignedPartnerId = assignedPartnerId;
    }

    public void setDistributedAt(Instant distributedAt) {
        this.distributedAt = distributedAt;
    }

    public LocalDate getSoldAt() {
        return soldAt;
    }

    public void setSoldAt(LocalDate soldAt) {
        this.soldAt = soldAt;
    }

    public BigDecimal getSaleCommissionRate() {
        return saleCommissionRate;
    }

    public void setSaleCommissionRate(BigDecimal saleCommissionRate) {
        this.saleCommissionRate = saleCommissionRate;
    }

    public BigDecimal getValorFipe() {
        return valorFipe;
    }

    public void setValorFipe(BigDecimal valorFipe) {
        this.valorFipe = valorFipe;
    }

    public boolean isStatusTransitionAllowed(VehicleStatus target) {
        return status == null || status.isTransitionAllowed(target);
    }

    public boolean isSold() {
        return status == VehicleStatus.SOLD;
    }

    public int calulatePurchaseTime() {
        if (createdAt == null) {
            return 0;
        }
        LocalDate now = LocalDate.now();
        LocalDate startDate = createdAt
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate endDate = soldAt != null ? soldAt : now;
        return (int) ChronoUnit.DAYS.between(startDate, endDate);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(id, vehicle.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
