package br.com.carreselling.application.service;

import br.com.carreselling.common.UuidGenerator;
import br.com.carreselling.application.service.model.VehicleDetail;
import br.com.carreselling.application.service.model.VehicleSummary;
import br.com.carreselling.application.service.model.VehicleTaxes;
import br.com.carreselling.domain.exception.ConflictException;
import br.com.carreselling.domain.exception.InvalidStateException;
import br.com.carreselling.domain.exception.NotFoundException;
import br.com.carreselling.domain.model.Brand;
import br.com.carreselling.domain.model.Color;
import br.com.carreselling.domain.model.Partner;
import br.com.carreselling.domain.model.SupplierSource;
import br.com.carreselling.domain.model.Vehicle;
import br.com.carreselling.domain.model.VehicleModel;
import br.com.carreselling.domain.model.VehicleStatus;
import br.com.carreselling.domain.repository.*;
import br.com.carreselling.infrastructure.storage.DocumentStorage;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class VehicleService implements IVehicleService {

    private static final String PLATE_REGEX = "^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$";

    private final VehicleRepository vehicleRepository;
    private final VehicleOnServiceHistoryRepository vehicleOnServiceHistoryRepository;
    private final DocumentRepository documentRepository;
    private final DocumentStorage documentStorage;
    private final ServiceRepository serviceRepository;
    private final PartnerRepository partnerRepository;
    private final BrandRepository brandRepository;
    private final ColorRepository colorRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final VehicleSalesCalculator salesCalculator;
    private final ServiceOnVehicleService serviceOnVehicleService;
    private final IPaymentService paymentService;

    public VehicleService(VehicleRepository vehicleRepository, VehicleOnServiceHistoryRepository vehicleOnServiceHistoryRepository,
                          DocumentRepository documentRepository,
                          DocumentStorage documentStorage,
                          ServiceRepository serviceRepository,
                          PartnerRepository partnerRepository,
                          BrandRepository brandRepository,
                          ColorRepository colorRepository,
                          VehicleModelRepository vehicleModelRepository,
                          VehicleSalesCalculator salesCalculator,
                          ServiceOnVehicleService serviceOnVehicleService,
                          IPaymentService paymentService) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleOnServiceHistoryRepository = vehicleOnServiceHistoryRepository;
        this.documentRepository = documentRepository;
        this.documentStorage = documentStorage;
        this.serviceRepository = serviceRepository;
        this.partnerRepository = partnerRepository;
        this.brandRepository = brandRepository;
        this.colorRepository = colorRepository;
        this.vehicleModelRepository = vehicleModelRepository;
        this.salesCalculator = salesCalculator;
        this.serviceOnVehicleService = serviceOnVehicleService;
        this.paymentService = paymentService;
    }

    @Override
    public UUID createVehicle(int companyId,
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
                              BigDecimal valorFipe) {
        String normalizedPlate = normalizePlate(licensePlate);
        String normalizedRenavam = normalizeOptionalText(renavam);
        String normalizedVin = normalizeOptionalText(vin);
        String normalizedColor = normalizeColor(color);
        String normalizedBrand = normalizeOptionalText(brand);
        String normalizedModel = normalizeOptionalText(model);
        validatePlate(normalizedPlate);
        validateRequiredMoney(purchasePrice, "purchasePrice");
        BigDecimal normalizedFreight = freightCost == null ? BigDecimal.ZERO : freightCost;
        validateOptionalMoney(normalizedFreight, "freightCost");
        BigDecimal normalizedCommission = purchaseCommission == null ? BigDecimal.ZERO : purchaseCommission;
        validateOptionalMoney(normalizedCommission, "purchaseCommission");
        Instant now = Instant.now();
        Optional<Vehicle> existingByPlate = vehicleRepository.findVehicleByLicensePlate(companyId, normalizedPlate);
        UUID currentVehicleId = existingByPlate.map(Vehicle::getId).orElse(null);
        validateUniqueRenavamAndVin(companyId, normalizedRenavam, normalizedVin, currentVehicleId);
        resolveColor(companyId, normalizedColor, now);
        Brand brandEntity = resolveBrand(companyId, normalizedBrand, now);
        VehicleModel modelEntity = resolveModel(companyId, brandEntity.getId(), normalizedModel, now);
        if (existingByPlate.isPresent()) {
            Vehicle existingVehicle = existingByPlate.get();
            existingVehicle.setRenavam(normalizedRenavam);
            existingVehicle.setVin(normalizedVin);
            existingVehicle.updateDetails(
                    year,
                    normalizedColor,
                    normalizedModel,
                    normalizedBrand,
                    supplierSource,
                    purchasePrice,
                    normalizedFreight,
                    normalizedCommission
            );
            existingVehicle.setBrandId(brandEntity.getId());
            existingVehicle.setModelId(modelEntity.getId());
            existingVehicle.setValorFipe(valorFipe);
            existingVehicle.setUpdatedAt(now);
            existingVehicle.ensureDistributionInvariant();
            vehicleRepository.updateVehicle(companyId, existingVehicle);
            return existingVehicle.getId();
        }
        Vehicle vehicle = new Vehicle(
                UuidGenerator.generate(),
                companyId,
                normalizedPlate,
                normalizedRenavam,
                normalizedVin,
                year,
                normalizedColor,
                normalizedModel,
                normalizedBrand,
                brandEntity.getId(),
                modelEntity.getId(),
                supplierSource,
                purchasePrice,
                normalizedFreight,
                normalizedCommission,
                null,
                valorFipe,
                null,
                null,
                VehicleStatus.IN_LOT,
                null,
                null,
                null,
                null,
                now,
                now
        );
        vehicle.ensureDistributionInvariant();
        vehicleRepository.saveVehicle(companyId, vehicle);
        return vehicle.getId();
    }

    private void validateUniqueRenavamAndVin(int companyId, String renavam, String vin, UUID currentVehicleId) {
        if (StringUtils.hasText(renavam)) {
            vehicleRepository.findVehicleByRenavam(companyId, renavam)
                    .ifPresent(existing -> {
                        if (currentVehicleId == null || !existing.getId().equals(currentVehicleId)) {
                            throw new ConflictException("Renavam already registered");
                        }
                    });
        }
        if (StringUtils.hasText(vin)) {
            vehicleRepository.findVehicleByVin(companyId, vin)
                    .ifPresent(existing -> {
                        if (currentVehicleId == null || !existing.getId().equals(currentVehicleId)) {
                            throw new ConflictException("VIN already registered");
                        }
                    });
        }
    }

    @Override
    public VehicleDetail getVehicle(int companyId, UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findVehicleById(companyId, vehicleId)
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        BigDecimal servicesTotal = vehicleRepository.findVehicleServicesTotalByVehicleId(companyId, vehicleId);
        int documentsCount = vehicleRepository.countVehicleDocumentsByVehicleId(companyId, vehicleId);
        BigDecimal totalCost = vehicle.getPurchasePrice()
                .add(vehicle.getFreightCost())
                .add(servicesTotal);
        String partnerName = resolvePartnerName(companyId, vehicle.getAssignedPartnerId());
        BigDecimal purchaseCommission = vehicle.getPurchaseCommission() == null
                ? BigDecimal.ZERO
                : vehicle.getPurchaseCommission();
        boolean onService = serviceRepository.existsOpenServiceByVehicleId(companyId, vehicleId);
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
                vehicle.getValorFipe(),
                vehicle.getPurchaseInvoiceDocumentId(),
                vehicle.getPurchasePaymentReceiptDocumentId(),
                vehicle.getStatus(),
                onService,
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
    public void updateSellingPrice(int companyId, UUID vehicleId, BigDecimal sellingPrice) {
        validateRequiredMoney(sellingPrice, "sellingPrice");
        Vehicle vehicle = vehicleRepository.findVehicleById(companyId, vehicleId)
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));

        vehicle.transitionStatus(VehicleStatus.SOLD);
        vehicle.updateSellingPrice(sellingPrice);
        stampSoldAt(vehicle);
        stampCommissionRate(companyId, vehicle);
        vehicle.setUpdatedAt(Instant.now());
        vehicleRepository.updateVehicle(companyId, vehicle);
    }

    @Override
    public VehicleTaxes getVehicleTaxes(int companyId, UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findVehicleById(companyId, vehicleId)
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
    public List<VehicleSummary> listVehicles(int companyId, VehicleStatus status, String query, Boolean isOnService, int page, int size) {
        if (size > 20) {
            size = 20;
        }
        int offset = Math.max(page, 0) * Math.max(size, 1);
        List<Vehicle> vehicles = vehicleRepository.findVehicleByFilter(companyId, status, query, isOnService, offset, size);
        return vehicles.stream()
                .map(vehicle -> {
                    BigDecimal servicesTotal = vehicleRepository.findVehicleServicesTotalByVehicleId(companyId, vehicle.getId());
                    BigDecimal totalCost = vehicle.getPurchasePrice()
                            .add(vehicle.getFreightCost())
                            .add(servicesTotal);
                    BigDecimal sellingPrice = vehicle.getSellingPrice();
                    BigDecimal profitMargin = sellingPrice == null
                            ? null
                            : sellingPrice.subtract(totalCost);
                    Integer purchaseTimeDays = vehicle.calulatePurchaseTime();

                    boolean serviceOpened = serviceRepository.existsOpenServiceByVehicleId(companyId, vehicle.getId());
                    return new VehicleSummary(
                            vehicle.getId(),
                            vehicle.getLicensePlate(),
                            vehicle.getBrand(),
                            vehicle.getModel(),
                            vehicle.getYear(),
                            vehicle.getStatus(),
                            serviceOpened,
                            sellingPrice,
                            totalCost,
                            profitMargin,
                            purchaseTimeDays,
                            servicesTotal,
                            serviceOnVehicleService.calculateTotalServiceDays(companyId, vehicle.getId())
                    );
                })
                .filter(vehicle -> isOnService == null || vehicle.onService() == isOnService)
                .toList();
    }

    @Override
    public long countVehicles(int companyId, VehicleStatus status, String query, Boolean onService) {
        return vehicleRepository.countVehicleByFilter(companyId, status, query, onService);
    }

    @Override
    public void updateVehicle(int companyId,
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
                              BigDecimal valorFipe) {
        validateRequiredMoney(purchasePrice, "purchasePrice");
        BigDecimal normalizedFreight = freightCost == null ? BigDecimal.ZERO : freightCost;
        validateOptionalMoney(normalizedFreight, "freightCost");
        validateOptionalMoney(purchaseCommission, "purchaseCommission");
        Vehicle vehicle = vehicleRepository.findVehicleById(companyId, vehicleId)
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        validateDocumentLink(companyId, vehicleId, invoiceDocumentId);
        validateDocumentLink(companyId, vehicleId, paymentReceiptDocumentId);
        Instant now = Instant.now();
        String normalizedColor = normalizeColor(color);
        resolveColor(companyId, normalizedColor, now);
        String normalizedBrand = normalizeOptionalText(brand);
        String normalizedModel = normalizeOptionalText(model);
        Brand brandEntity = resolveBrand(companyId, normalizedBrand, now);
        VehicleModel modelEntity = resolveModel(companyId, brandEntity.getId(), normalizedModel, now);
        vehicle.updateDetails(
                year,
                normalizedColor,
                normalizedModel,
                normalizedBrand,
                supplierSource,
                purchasePrice,
                normalizedFreight,
                purchaseCommission
        );
        vehicle.updateLinkedDocuments(invoiceDocumentId, paymentReceiptDocumentId);
        vehicle.setBrandId(brandEntity.getId());
        vehicle.setModelId(modelEntity.getId());
        vehicle.setValorFipe(valorFipe);
        vehicle.setUpdatedAt(Instant.now());
        vehicle.ensureDistributionInvariant();
        stampCommissionRate(companyId, vehicle);
        if (vehicle.isSold()) {
            stampSoldAt(vehicle);
        }
        vehicleRepository.updateVehicle(companyId, vehicle);
    }

    @Override
    public void transitionStatus(int companyId, UUID vehicleId, VehicleStatus targetStatus, UUID assignedPartnerId) {
        Vehicle vehicle = vehicleRepository.findVehicleById(companyId, vehicleId)
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        if (!vehicle.isStatusTransitionAllowed(targetStatus)) {
            throw new InvalidStateException("Invalid status transition.");
        }
        if (targetStatus == VehicleStatus.DISTRIBUTED && assignedPartnerId == null && vehicle.getAssignedPartnerId() == null) {
            throw new InvalidStateException("Assigned partner is required when distributing a vehicle.");
        }
        if (targetStatus == VehicleStatus.SOLD && vehicle.getSellingPrice() == null) {
            throw new InvalidStateException("Selling price is required before marking as sold.");
        }
        if ((targetStatus == VehicleStatus.DISTRIBUTED || targetStatus == VehicleStatus.SOLD)
                && assignedPartnerId != null) {
            partnerRepository.findPartnerById(companyId, assignedPartnerId)
                    .orElseThrow(() -> new NotFoundException("Partner not found"));
        }
        UUID partner = assignedPartnerId != null ? assignedPartnerId : vehicle.getAssignedPartnerId();
        vehicle.transitionStatus(targetStatus, partner);
        if (targetStatus == VehicleStatus.DISTRIBUTED) {
            if (vehicle.getDistributedAt() == null) {
                vehicle.setDistributedAt(Instant.now());
            }
            if (partner != null) {
                partnerRepository.findPartnerById(companyId, partner)
                        .ifPresent(p -> vehicle.setSaleCommissionRate(p.getCommissionRate()));
            }
        }
        if (targetStatus == VehicleStatus.SOLD) {
            stampSoldAt(vehicle);
            if (partner != null) {
                partnerRepository.findPartnerById(companyId, partner)
                        .ifPresent(p -> vehicle.setSaleCommissionRate(p.getCommissionRate()));
            } else {
                stampCommissionRate(companyId, vehicle);
            }
        }
        vehicle.setUpdatedAt(Instant.now());
        vehicle.ensureDistributionInvariant();
        vehicleRepository.updateVehicle(companyId, vehicle);
    }

    @Override
    public void assignPartner(int companyId, UUID vehicleId, UUID partnerId) {
        Vehicle vehicle = vehicleRepository.findVehicleById(companyId, vehicleId)
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        Partner partner = partnerRepository.findPartnerById(companyId, partnerId)
                .orElseThrow(() -> new NotFoundException("Partner not found"));
        vehicle.assignPartner(partner.getId());
        if (vehicle.getDistributedAt() == null) {
            vehicle.setDistributedAt(Instant.now());
        }
        vehicle.setSaleCommissionRate(partner.getCommissionRate());
        vehicle.setUpdatedAt(Instant.now());
        vehicle.ensureDistributionInvariant();
        vehicleRepository.updateVehicle(companyId, vehicle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVehicle(int companyId, UUID vehicleId) {
        vehicleRepository.findVehicleById(companyId, vehicleId)
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));

        List<String> storageKeys = documentRepository.findStorageKeyByVehicleId(companyId, vehicleId);
        deleteFormStorage(vehicleId, storageKeys);
        paymentService.deletePaymentsByVehicleId(companyId, vehicleId);
        vehicleOnServiceHistoryRepository.deleteByVehicleId(companyId, vehicleId);
        serviceRepository.deleteServicesByVehicleId(companyId, vehicleId);
        vehicleRepository.deleteVehicle(companyId, vehicleId);
    }

    private void deleteFormStorage(UUID vehicleId, List<String> storageKeys) {
        for (var storageKey : storageKeys) {
            try {
                documentStorage.delete(storageKey);
            } catch (Exception e) {
                log.warn("Storage object not found while deleting document.vehicleId={}, storageKey={}",
                        vehicleId, storageKey);
                throw e;
            }
        }
    }

    private Brand resolveBrand(int companyId, String brand, Instant now) {
        String normalized = normalizeOptionalText(brand);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("brand: required.");
        }
        return brandRepository.findBrandByName(companyId, normalized)
                .orElseGet(() -> brandRepository.saveBrand(companyId, new Brand(
                        UuidGenerator.generate(),
                        companyId,
                        normalized,
                        now,
                        now
                )));
    }

    private void resolveColor(int companyId, String color, Instant now) {
        if (!StringUtils.hasText(color)) {
            throw new IllegalArgumentException("color: required.");
        }
        colorRepository.findColorByName(companyId, color)
                .orElseGet(() -> colorRepository.saveColor(companyId, new Color(
                        UuidGenerator.generate(),
                        companyId,
                        color,
                        now,
                        now
                )));
    }

    private VehicleModel resolveModel(int companyId, UUID brandId, String model, Instant now) {
        String normalized = normalizeOptionalText(model);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("model: required.");
        }
        return vehicleModelRepository.findModelByBrandIdAndName(companyId, brandId, normalized)
                .orElseGet(() -> vehicleModelRepository.saveModel(companyId, new VehicleModel(
                        UuidGenerator.generate(),
                        companyId,
                        brandId,
                        normalized,
                        now,
                        now
                )));
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

    private void validateDocumentLink(int companyId, UUID vehicleId, UUID documentId) {
        if (documentId == null) {
            return;
        }
        documentRepository.findDocumentById(companyId, documentId)
                .filter(document -> document.getVehicleId().equals(vehicleId))
                .orElseThrow(() -> new NotFoundException("Document not found for vehicle."));
    }

    private String normalizePlate(String plate) {
        return plate == null ? null : plate.trim().toUpperCase();
    }

    private String normalizeOptionalText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeColor(String value) {
        String normalized = normalizeOptionalText(value);
        return normalized == null ? null : normalized.toUpperCase();
    }

    private String resolvePartnerName(int companyId, UUID partnerId) {
        if (partnerId == null) {
            return null;
        }
        Optional<Partner> partner = partnerRepository.findPartnerById(companyId, partnerId);
        return partner.map(Partner::getName).orElse(null);
    }

    private void stampSoldAt(Vehicle vehicle) {
        if (vehicle.getSoldAt() == null) {
            vehicle.setSoldAt(LocalDate.now());
        }
    }

    private void stampCommissionRate(int companyId, Vehicle vehicle) {
        UUID partnerId = vehicle.getAssignedPartnerId();
        if (partnerId != null) {
            partnerRepository.findPartnerById(companyId, partnerId)
                    .ifPresent(p -> vehicle.setSaleCommissionRate(p.getCommissionRate()));
        }
    }

}
