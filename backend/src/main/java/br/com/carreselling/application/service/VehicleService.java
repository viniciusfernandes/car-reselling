package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.VehicleDetail;
import br.com.carreselling.application.service.model.VehicleSummary;
import br.com.carreselling.application.service.model.VehicleTaxes;
import br.com.carreselling.domain.exception.ConflictException;
import br.com.carreselling.domain.exception.InvalidStateException;
import br.com.carreselling.domain.exception.NotFoundException;
import br.com.carreselling.domain.model.Partner;
import br.com.carreselling.domain.model.SupplierSource;
import br.com.carreselling.domain.model.Vehicle;
import br.com.carreselling.domain.model.VehicleStatus;
import br.com.carreselling.domain.repository.DocumentRepository;
import br.com.carreselling.domain.repository.PartnerRepository;
import br.com.carreselling.domain.repository.VehicleRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class VehicleService implements IVehicleService {

    private static final String PLATE_REGEX = "^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$";

    private final VehicleRepository vehicleRepository;
    private final DocumentRepository documentRepository;
    private final PartnerRepository partnerRepository;
    private final VehicleSalesCalculator salesCalculator;

    public VehicleService(VehicleRepository vehicleRepository,
                          DocumentRepository documentRepository,
                          PartnerRepository partnerRepository,
                          VehicleSalesCalculator salesCalculator) {
        this.vehicleRepository = vehicleRepository;
        this.documentRepository = documentRepository;
        this.partnerRepository = partnerRepository;
        this.salesCalculator = salesCalculator;
    }

    @Override
    public UUID createVehicle(String licensePlate,
                              String renavam,
                              String vin,
                              int year,
                              String color,
                              String model,
                              String brand,
                              SupplierSource supplierSource,
                              BigDecimal purchasePrice,
                              BigDecimal freightCost,
                              BigDecimal purchaseCommission) {
        String normalizedPlate = normalizePlate(licensePlate);
        String normalizedRenavam = normalizeOptionalText(renavam);
        String normalizedVin = normalizeOptionalText(vin);
        validatePlate(normalizedPlate);
        validateRequiredMoney(purchasePrice, "purchasePrice");
        BigDecimal normalizedFreight = freightCost == null ? BigDecimal.ZERO : freightCost;
        validateOptionalMoney(normalizedFreight, "freightCost");
        BigDecimal normalizedCommission = purchaseCommission == null ? BigDecimal.ZERO : purchaseCommission;
        validateOptionalMoney(normalizedCommission, "purchaseCommission");
        vehicleRepository.findVehicleByLicensePlate(normalizedPlate)
            .ifPresent(existing -> {
                throw new ConflictException("License plate already registered");
            });
        if (StringUtils.hasText(normalizedRenavam)) {
            vehicleRepository.findVehicleByRenavam(normalizedRenavam)
                .ifPresent(existing -> {
                    throw new ConflictException("Renavam already registered");
                });
        }
        if (StringUtils.hasText(normalizedVin)) {
            vehicleRepository.findVehicleByVin(normalizedVin)
                .ifPresent(existing -> {
                    throw new ConflictException("VIN already registered");
                });
        }
        Instant now = Instant.now();
        Vehicle vehicle = new Vehicle(
            UUID.randomUUID(),
            normalizedPlate,
            normalizedRenavam,
            normalizedVin,
            year,
            color,
            model,
            brand,
            supplierSource,
            purchasePrice,
            normalizedFreight,
            normalizedCommission,
            null,
            null,
            null,
            VehicleStatus.IN_LOT,
            null,
            null,
            now,
            now
        );
        vehicle.ensureDistributionInvariant();
        vehicleRepository.saveVehicle(vehicle);
        return vehicle.getId();
    }

    @Override
    public VehicleDetail getVehicle(UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId)
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        BigDecimal servicesTotal = vehicleRepository.findVehicleServicesTotalByVehicleId(vehicleId);
        int documentsCount = vehicleRepository.countVehicleDocumentsByVehicleId(vehicleId);
        BigDecimal totalCost = vehicle.getPurchasePrice()
            .add(vehicle.getFreightCost())
            .add(servicesTotal);
        String partnerName = resolvePartnerName(vehicle.getAssignedPartnerId());
        BigDecimal purchaseCommission = vehicle.getPurchaseCommission() == null
            ? BigDecimal.ZERO
            : vehicle.getPurchaseCommission();
        return new VehicleDetail(
            vehicle.getId(),
            vehicle.getLicensePlate(),
            vehicle.getRenavam(),
            vehicle.getVin(),
            vehicle.getYear(),
            vehicle.getColor(),
            vehicle.getModel(),
            vehicle.getBrand(),
            vehicle.getSupplierSource(),
            vehicle.getPurchasePrice(),
            vehicle.getFreightCost(),
            purchaseCommission,
            vehicle.getSellingPrice(),
            vehicle.getPurchaseInvoiceDocumentId(),
            vehicle.getPurchasePaymentReceiptDocumentId(),
            vehicle.getStatus(),
            vehicle.getAssignedPartnerId(),
            partnerName,
            servicesTotal,
            totalCost,
            documentsCount,
            vehicle.getCreatedAt(),
            vehicle.getUpdatedAt(),
            vehicle.getDistributedAt()
        );
    }

    @Override
    public void updateSellingPrice(UUID vehicleId, BigDecimal sellingPrice) {
        validateRequiredMoney(sellingPrice, "sellingPrice");
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId)
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        if (vehicle.getStatus() != VehicleStatus.DISTRIBUTED) {
            throw new InvalidStateException("Selling price can only be updated when distributed.");
        }
        vehicle.updateSellingPrice(sellingPrice);
        vehicle.setUpdatedAt(Instant.now());
        vehicleRepository.updateVehicle(vehicle);
    }

    @Override
    public VehicleTaxes getVehicleTaxes(UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId)
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        if (vehicle.getSellingPrice() == null) {
            return new VehicleTaxes(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
            );
        }
        BigDecimal baseProfit = vehicle.getSellingPrice().subtract(vehicle.getPurchasePrice());
        BigDecimal taxableMargin = baseProfit.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : baseProfit;
        VehicleSalesCalculator.TaxBreakdown taxes = salesCalculator.calculateTaxes(
            vehicle.getSellingPrice(),
            taxableMargin
        );
        return new VehicleTaxes(
            taxes.icms(),
            taxes.pis(),
            taxes.cofins(),
            taxes.csll(),
            taxes.irpj(),
            taxes.totalTaxes()
        );
    }

    @Override
    public List<VehicleSummary> listVehicles(VehicleStatus status, String query, int page, int size) {
        int offset = Math.max(page, 0) * Math.max(size, 1);
        List<Vehicle> vehicles = vehicleRepository.findVehicleByFilter(status, query, offset, size);
        return vehicles.stream()
            .map(vehicle -> {
                BigDecimal servicesTotal = vehicleRepository.findVehicleServicesTotalByVehicleId(vehicle.getId());
                BigDecimal totalCost = vehicle.getPurchasePrice()
                    .add(vehicle.getFreightCost())
                    .add(servicesTotal);
                String partnerName = resolvePartnerName(vehicle.getAssignedPartnerId());
                BigDecimal purchaseCommission = vehicle.getPurchaseCommission() == null
                    ? BigDecimal.ZERO
                    : vehicle.getPurchaseCommission();

                return new VehicleSummary(
                    vehicle.getId(),
                    vehicle.getLicensePlate(),
                    vehicle.getBrand(),
                    vehicle.getModel(),
                    vehicle.getYear(),
                    vehicle.getStatus(),
                    vehicle.getPurchasePrice(),
                    purchaseCommission,
                    servicesTotal,
                    totalCost,
                    partnerName,
                    vehicle.calculateTotalYardDays()
                );
            })
            .toList();
    }

    @Override
    public long countVehicles(VehicleStatus status, String query) {
        return vehicleRepository.countVehicleByFilter(status, query);
    }

    @Override
    public void updateVehicle(UUID vehicleId,
                              int year,
                              String color,
                              String model,
                              String brand,
                              SupplierSource supplierSource,
                              BigDecimal purchasePrice,
                              BigDecimal freightCost,
                              BigDecimal purchaseCommission,
                              UUID invoiceDocumentId,
                              UUID paymentReceiptDocumentId) {
        validateRequiredMoney(purchasePrice, "purchasePrice");
        validateRequiredMoney(freightCost, "freightCost");
        validateOptionalMoney(purchaseCommission, "purchaseCommission");
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId)
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        validateDocumentLink(vehicleId, invoiceDocumentId);
        validateDocumentLink(vehicleId, paymentReceiptDocumentId);
        vehicle.updateDetails(
            year,
            color,
            model,
            brand,
            supplierSource,
            purchasePrice,
            freightCost,
            purchaseCommission
        );
        vehicle.updateLinkedDocuments(invoiceDocumentId, paymentReceiptDocumentId);
        vehicle.setUpdatedAt(Instant.now());
        vehicle.ensureDistributionInvariant();
        vehicleRepository.updateVehicle(vehicle);
    }

    @Override
    public void transitionStatus(UUID vehicleId, VehicleStatus targetStatus, UUID assignedPartnerId) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId)
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        if (!isAllowedTransition(vehicle.getStatus(), targetStatus)) {
            throw new InvalidStateException("Invalid status transition.");
        }
        if (targetStatus == VehicleStatus.DISTRIBUTED && assignedPartnerId == null && vehicle.getAssignedPartnerId() == null) {
            throw new InvalidStateException("Assigned partner is required when distributing a vehicle.");
        }
        if (targetStatus == VehicleStatus.SOLD && vehicle.getSellingPrice() == null) {
            throw new InvalidStateException("Selling price is required before marking as sold.");
        }
        if (targetStatus == VehicleStatus.DISTRIBUTED && assignedPartnerId != null) {
            partnerRepository.findPartnerById(assignedPartnerId)
                .orElseThrow(() -> new NotFoundException("Partner not found"));
        }
        UUID partner = assignedPartnerId != null ? assignedPartnerId : vehicle.getAssignedPartnerId();
        vehicle.transitionStatus(targetStatus, partner);
        if (targetStatus == VehicleStatus.DISTRIBUTED && vehicle.getDistributedAt() == null) {
            vehicle.setDistributedAt(Instant.now());
        }
        vehicle.setUpdatedAt(Instant.now());
        vehicle.ensureDistributionInvariant();
        vehicleRepository.updateVehicle(vehicle);
    }

    @Override
    public void assignPartner(UUID vehicleId, UUID partnerId) {
        Vehicle vehicle = vehicleRepository.findVehicleById(vehicleId)
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        Partner partner = partnerRepository.findPartnerById(partnerId)
            .orElseThrow(() -> new NotFoundException("Partner not found"));
        vehicle.assignPartner(partner.getId());
        if (vehicle.getDistributedAt() == null) {
            vehicle.setDistributedAt(Instant.now());
        }
        vehicle.setUpdatedAt(Instant.now());
        vehicle.ensureDistributionInvariant();
        vehicleRepository.updateVehicle(vehicle);
    }

    private void validatePlate(String plate) {
        if (plate == null || !plate.matches(PLATE_REGEX)) {
            throw new IllegalArgumentException("licensePlate: invalid format.");
        }
    }

    private void validateRequiredMoney(BigDecimal value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + ": required.");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(field + ": cannot be negative.");
        }
    }

    private void validateOptionalMoney(BigDecimal value, String field) {
        if (value == null) {
            return;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(field + ": cannot be negative.");
        }
    }

    private void validateDocumentLink(UUID vehicleId, UUID documentId) {
        if (documentId == null) {
            return;
        }
        documentRepository.findDocumentById(documentId)
            .filter(document -> document.getVehicleId().equals(vehicleId))
            .orElseThrow(() -> new NotFoundException("Document not found for vehicle."));
    }

    private String normalizePlate(String plate) {
        return plate == null ? null : plate.trim().toUpperCase();
    }

    private String normalizeOptionalText(String value) {
        return value == null ? null : value.trim();
    }

    private String resolvePartnerName(UUID partnerId) {
        if (partnerId == null) {
            return null;
        }
        Optional<Partner> partner = partnerRepository.findPartnerById(partnerId);
        return partner.map(Partner::getName).orElse(null);
    }

    private boolean isAllowedTransition(VehicleStatus current, VehicleStatus target) {
        if (current == target) {
            return true;
        }
        return switch (current) {
            case IN_LOT -> target == VehicleStatus.IN_SERVICE;
            case IN_SERVICE -> target == VehicleStatus.READY_FOR_DISTRIBUTION;
            case READY_FOR_DISTRIBUTION -> target == VehicleStatus.DISTRIBUTED;
            case DISTRIBUTED -> target == VehicleStatus.SOLD;
            case SOLD -> false;
        };
    }
}
